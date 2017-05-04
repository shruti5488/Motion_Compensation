                Segmented and Gaze Controlled decompression for streaming displays such as VR 
           
Application of block-based motion detection and compression using DCT transform by adaptively encoding/decoding the content based on sematics in the video. Emulated the VR effect using view dependent gaze 
to control decoding.

To formally describe the project:
1.	Sematic Layering of video: There is a pre processing step where you will analyze the input video, break it into foreground/background layers. This is not required to work in real time, and semantic accuracy may vary.
2.	Compressing of layers: Each of the foreground/background layers will need to be stored in a compressible manner so that later access can  decide what needs to be read/streamed to the player and displayed.  This is not required  to work in real time, and file format for storing the data is up to to you to decide – though an example file format has been suggested.
3.	Displaying of video: Your player should read the compressed video file and display the video per quantization inputs to simulate bandwidth distribution among layers. Foreground layers should be ideally more clearly visible than background layers .
4.	Applying gaze control: We don't have Head mounted VR displays to work with to see the value of this  effect, but we can certainly simulate gaze based control by using a mouse pointer. With your mouse location acting as "gaze direction" you want decode/display a local area around the mouse location will the best clarity (no quantization) compared to other layered areas.
