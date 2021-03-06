(ns mcms.camera
  (:use [mcms opencv])
  (:import [java.awt.event ActionListener KeyAdapter]
	   [javax.swing JFrame JLabel Timer]
	   [name.audet.samuel.javacv OpenCVFrameGrabber]))

(def frame-rate (int 1000/30))

(defonce *selected* (atom nil))

(defn process-image [#^cxcore$IplImage image]
    (draw-face-rects image (compute-faces image)))

(defn key-listener [image db]
  (proxy [KeyAdapter] [] 
    (keyTyped [e]
      (println "listening!!!")
      (reset! *selected* 1)
      #_(println image)
      (-> e (.getSource) (.setVisible false)))))

(defn debug [db]
  (def  grabber (make-grabber))
  (def image (atom (.grab #^OpenCVFrameGrabber grabber)))
  (def frame (make-frame "Debugging" image db)))

(defn end-debug []
  (.stop #^OpenCVFrameGrabber grabber))

(defn capture-action [#^JFrame frame, #^OpenCVFrameGrabber grabber, #^IplImage image login-promise]
  (proxy [ActionListener] []
    (actionPerformed [e]
        (if (.isVisible  frame)
          (do
            (reset! image (.grab grabber))
            (process-image @image)
	    (.clearMem storage)
            (.repaint frame))            
          (do 
            (println "Done!")
            (.stop grabber)
            (-> e (.getSource) (.stop))
            (deliver login-promise {:uid 1, :selected @*selected*}))))))

(defn face-detect [db login-promise]
  (let [grabber (make-grabber)
        image (atom (.grab grabber))
	frame (make-frame "Login" image (key-listener image db))
        bufferedImage (.getBufferedImage @image)
        timer (Timer. frame-rate (capture-action frame grabber image login-promise))]
     (.start timer)
     (.setSize frame (.getWidth bufferedImage) (.getHeight bufferedImage))))

