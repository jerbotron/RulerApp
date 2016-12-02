Author: Jeremy Wang
Date: 11/19/2017

TinderRuler App

The TinderRuler app operates in 3 modes:
- 1) Default
- 2) Measure
- 3) Game

Icon buttons at the bottom of the screen are used to enter each mode, DEFAULT mode is entered by de-selecting the other two modes. 

1) Default Mode:
- Simple background ruler app showing inches, 1/2 in, 1/4 in, 1/8 in, and 1/16 in.

2) Measure Mode:
- User can use touch to drag a different colored marker for precise measurements.
- The exact measurement rounded to 2 decimal digits is displayed beside the marker in real time.
- Each touch event calculates the differential bewteen initial touched point to current point, so the marker does not follow user's finger location but the delta per touch. 
- Marker stops automatically at 0 and MAX_HEIGHT of the screen size. 

3) Game Mode:
- Motivation: during my initial conversation with Levi, he mentioned that Android animation experience was preferred to have for this position. So I thought this would be a good opportunity to quickly learn some basic animations in Android. I admit I spent longer than 1-2 hrs on this project, but I think it was worth the experience if I were to move forward in the interview process! 
- The game is a very straight forward "Fieldgoal kicking game". A football appears each round at a random start position at the 3.5 inch mark and a user flicks the screen to imitate a "kick" and the speed/length of the user touch event will determine the velocity applied to the "kick". If the ball goes through the goal post then a vibration will go off indicating that the field goal is GOOD! But beware of the WIND!

Notable Android APIs Used:
- SurfaceView used to run the app in FULLSCREEN mode and to capture screen canvas so drawings and animations can be made to the view through the canvas.
- Canvas, Paint, Bitmap -> all used to draw objects to screen.
- Runnables/Thread -> used to refresh/render the canvas in the background and post to the SurfaceView.
- Vibrator -> used to vibrate the device in game mode.