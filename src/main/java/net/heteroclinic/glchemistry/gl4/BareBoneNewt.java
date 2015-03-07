/**
 * @author Zhikai Wang
 * www.heteroclinic.net
 * Please read the accompanying LICENSE
 */
package net.heteroclinic.glchemistry.gl4;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.swing.SwingUtilities;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;


public class BareBoneNewt implements IInstancedRenderingView {

	protected float winScale = 0.1f;
	private static final float WIN_SCALE_MIN = 1e-3f;
	private static final float WIN_SCALE_MAX = 100f;
	private final FPSAnimator animator;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BareBoneNewt(null);
			}
		});
	}

	public BareBoneNewt(Renderer renderer) {
		//implements GLEventListener 
		//CuboidRendererWithShaderState renderer = new CuboidRendererWithShaderState(this);
		if (renderer!= null)
			renderer.setView(this);

		GLProfile prof = GLProfile.get(GLProfile.GL4);
		GLCapabilities caps = new GLCapabilities(prof);
		GLWindow glWindow = GLWindow.create(caps);

		if ( null != renderer ) {
			glWindow.addGLEventListener(renderer);
		}

		glWindow.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseEvent e) {
				float[] step = e.getRotation();
				if(step[1] > 0) {
					winScale *= 1.05;
					if(winScale > WIN_SCALE_MAX) winScale = WIN_SCALE_MAX;
				} else if(0 > step[1] ) {
					winScale *= 0.95;
					if(winScale < WIN_SCALE_MIN) winScale = WIN_SCALE_MIN;
				}
			}
		});

		glWindow.addWindowListener(new WindowAdapter() {

			@Override
			public void windowDestroyed(WindowEvent evt) {
				if(animator.isAnimating()) animator.stop();
			}

			@Override
			public void windowDestroyNotify(WindowEvent evt) {
				animator.stop();
			}
		});

    	animator = new FPSAnimator(glWindow, 60, true);
    	animator.start();

    	glWindow.setTitle("Instanced rendering experiment");
        glWindow.setSize(1024, 768);
        glWindow.setUndecorated(false);
        glWindow.setPointerVisible(true);
        glWindow.setVisible(true);
	}

	@Override
	public float getScale() {
		return winScale;
	}

}
