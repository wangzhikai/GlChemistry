package net.heteroclinic.glchemistry.gl4;

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;

import net.heteroclinic.computergraphics.math.glcomputable.FloatingOPsKeyAdapter;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class BareBoneRenderer implements GLEventListener {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BareBoneNewt(new BareBoneRenderer());
			}
		});
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		
		/* GL2 implementation
		System.out.println("SimpleFloatingOPsGLEventListener init called");
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		((GLLightingFunc) gl).glShadeModel(GLLightingFunc.GL_SMOOTH);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_CULL_FACE);
		((GL2) gl).glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		float ambient[] = { 0.2f, 0.2f, 0.2f, 1 };
		float position[] = { 1.0f, 10.0f, 5.7f, 1 };
		float intensity[] = { 1, 1, 1, 1 };
		float specColor[] = { 1, 1, 1, 1 };

		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		((GL2ES1) gl).glLightModelfv(GLLightingFunc.GL_AMBIENT, ambient, 0);
		gl.glEnable(GLLightingFunc.GL_LIGHT0);
		((GLLightingFunc) gl).glLightfv(GLLightingFunc.GL_LIGHT0,
				GLLightingFunc.GL_POSITION, position, 0);
		((GLLightingFunc) gl).glLightfv(GLLightingFunc.GL_LIGHT0,
				GLLightingFunc.GL_DIFFUSE, intensity, 0);
		((GLLightingFunc) gl).glLightfv(GLLightingFunc.GL_LIGHT0,
				GLLightingFunc.GL_SPECULAR, specColor, 0);

		gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
		((GL) gl).glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		hints_renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 16));
		hints_renderer.setColor(0f, 0f, 0f, 1.0f);
		
		glcamera.glinit(drawable);

		KeyListener floatingopesKeys = new FloatingOPsKeyAdapter();
	    if (drawable instanceof Window) {
				        Window window = (Window) drawable;
				        //window.addMouseListener(gearsMouse);
				        window.addKeyListener(floatingopesKeys);
				    } else if (GLProfile.isAWTAvailable() && drawable instanceof java.awt.Component) {
				        java.awt.Component comp = (java.awt.Component) drawable;
				        //new AWTMouseAdapter(gearsMouse, drawable).addTo(comp);
				        new AWTKeyAdapter(floatingopesKeys, drawable).addTo(comp);
	    }
	    */
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		
	}

}
