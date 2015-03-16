// Copyright 2014 JogAmp Community. All rights reserved.

#if __VERSION__ >= 130
  #define varying in
  out vec4 mgl_FragColor;
  #define texture2D texture
#else
  #define mgl_FragColor gl_FragColor   
#endif
uniform vec4 light_position;

varying vec4    frontColor;

void main (void) {
	//mgl_FragColor = frontColor; 
	mgl_FragColor = light_position;
	//!mgl_FragColor = vect4(1.0,0.0,0.0,1.0);
} 
