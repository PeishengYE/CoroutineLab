package main

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"flag"
	"html/template"
	"image"
	"fmt"
	"image/color"
	"image/draw"
	"image/jpeg"
	"os/exec"
	"log"
	"os"
	"io"
	"net/http"
	"strconv"
	"syscall"

)

var root = flag.String("root", ".", "file system path")
var count = 0

func main() {
	http.HandleFunc("/blue/", blueHandler)
	http.HandleFunc("/getJson",jsonHandler )
	http.HandleFunc("/image/", imageHandler)
	http.HandleFunc("/red/", redHandler)
	http.Handle("/", http.FileServer(http.Dir(*root)))
	log.Println("Listening on 8080")
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		log.Fatal("ListenAndServe:", err)
	}
}

func blueHandler(w http.ResponseWriter, r *http.Request) {
	m := image.NewRGBA(image.Rect(0, 0, 240, 240))
	blue := color.RGBA{0, 0, 255, 255}
	draw.Draw(m, m.Bounds(), &image.Uniform{blue}, image.ZP, draw.Src)

	var img image.Image = m
	writeImage(w, &img)
}

func callSystemCmd( cmdInput []string ) string  {
	var  osCmd *exec.Cmd
	if len(cmdInput) == 2{
		osCmd = exec.Command( cmdInput[0], cmdInput[1])
	}else if len(cmdInput) == 3{
		osCmd = exec.Command( cmdInput[0], cmdInput[1], cmdInput[2])
	}else if len(cmdInput) == 4{
		osCmd = exec.Command( cmdInput[0], cmdInput[1], cmdInput[2], cmdInput[3])

	}else if len(cmdInput) == 5{
		osCmd = exec.Command( cmdInput[0], cmdInput[1], cmdInput[2], cmdInput[3], cmdInput[4])
	    log.Println("command with ", cmdInput[0])

	}
		osCmd.Env = append(os.Environ(), "DISPLAY=:0.0")
		var out bytes.Buffer
		osCmd.Stdout = &out
		if err := osCmd.Start(); err != nil {
			log.Fatalf("osCmd.Start: %v", err)
			return "\n"
		}

		if err := osCmd.Wait(); err != nil {
			if exiterr, ok := err.(*exec.ExitError); ok {
				// The program has exited with an exit code != 0
				// This works on both Unix and Windows. Although package
				// syscall is generally platform dependent, WaitStatus is
				// defined for both Unix and Windows and in both cases has
				// an ExitStatus() method with the same signature.
				if status, ok := exiterr.Sys().(syscall.WaitStatus); ok {
					log.Printf("YEP-GO: Exit Status: %d", status.ExitStatus())
				}
			} else {
				log.Fatalf("osCmd.Wait: %v", err)
			}
			return "\n"
		}

		//fmt.Printf("%q\n", out.String() )
		return out.String()

}

type UseAll struct {
	Name    string `json:"username"`
	Surname string `json:"surname"`
	Year    int    `json:"created"`
}


func jsonHandler (w http.ResponseWriter, r *http.Request) {

    fmt.Println("jsonHandler()>>")
   //useall := UseAll{Name: "Mike", Surname: "Tsoukalos", Year: 2021}
   count ++
   testStr := "Nice work! with count: " + strconv.Itoa(count)

	// Regular Structure
	// Encoding JSON data -> Convert Go Structure to JSON record with fields
	jsonTmp, err := json.Marshal(&testStr)
	if err != nil {
		fmt.Println(err)
	} else {
		fmt.Printf("Value %s\n", jsonTmp)
	}

    w.Header().Set("Content-Type", "application/json") // <-- set the content-type header
    //io.Copy(w, jsonTmp )
	fmt.Fprintf(w,string(jsonTmp)) 
}


func imageHandler (w http.ResponseWriter, r *http.Request) {
	/*
	YEP note: somehow, it failed on Ziyi computer
 	    n := screenshot.NumActiveDisplays()
		bounds := screenshot.GetDisplayBounds(0)

 		img, err := screenshot.CaptureRect(bounds)
 		if err != nil {
 			panic(err)
 		}
 		//fileName := fmt.Sprintf("%d_%dx%d.png", i, bounds.Dx(), bounds.Dy())
 		fileName := fmt.Sprintf("screenshot_%dx%d.png",  bounds.Dx(), bounds.Dy())
 		file, _ := os.Create(fileName)
 		defer file.Close()
 		png.Encode(file, img)

 		fmt.Printf("Screen 0 : %v \"%s\"\n",  bounds, fileName)
		*/
        //import -window root root.png
        fmt.Println("imageHandler()>>")
		fileName_tmp := "/home/tech/screenshot.png"
		fileName := "screenshot.small.png"
		//cmd := []string{"import", "-window", "root", fileName_tmp}
		cmd := []string{"su", "tech", "-c", "DISPLAY=:0.0 import -window root /home/tech/screenshot.png"}
        callSystemCmd(cmd)
		cmd = []string{"convert", "-resize", "800x600",fileName_tmp, fileName}
        callSystemCmd(cmd)


    imgFile, err := os.Open(fileName)
    if err != nil {
        log.Fatal(err) // perhaps handle this nicer
    }
    defer imgFile.Close()
    w.Header().Set("Content-Type", "image/png") // <-- set the content-type header
    io.Copy(w, imgFile)
}

func redHandler(w http.ResponseWriter, r *http.Request) {
	m := image.NewRGBA(image.Rect(0, 0, 240, 240))
	blue := color.RGBA{255, 0, 0, 255}
	draw.Draw(m, m.Bounds(), &image.Uniform{blue}, image.ZP, draw.Src)

	var img image.Image = m
	writeImageWithTemplate(w, &img)
}

var ImageTemplate string = `<!DOCTYPE html>
<html lang="en"><head></head>
<body><img src="data:image/jpg;base64,{{.Image}}"></body>`

// Writeimagewithtemplate encodes an image 'img' in jpeg format and writes it into ResponseWriter using a template.
func writeImageWithTemplate(w http.ResponseWriter, img *image.Image) {

	buffer := new(bytes.Buffer)
	if err := jpeg.Encode(buffer, *img, nil); err != nil {
		log.Fatalln("unable to encode image.")
	}

	str := base64.StdEncoding.EncodeToString(buffer.Bytes())
	if tmpl, err := template.New("image").Parse(ImageTemplate); err != nil {
		log.Println("unable to parse image template.")
	} else {
		data := map[string]interface{}{"Image": str}
		if err = tmpl.Execute(w, data); err != nil {
			log.Println("unable to execute template.")
		}
	}
}

// writeImage encodes an image 'img' in jpeg format and writes it into ResponseWriter.
func writeImage(w http.ResponseWriter, img *image.Image) {

	buffer := new(bytes.Buffer)
	if err := jpeg.Encode(buffer, *img, nil); err != nil {
		log.Println("unable to encode image.")
	}

	w.Header().Set("Content-Type", "image/jpeg")
	w.Header().Set("Content-Length", strconv.Itoa(len(buffer.Bytes())))
	if _, err := w.Write(buffer.Bytes()); err != nil {
		log.Println("unable to write image.")
	}
}
