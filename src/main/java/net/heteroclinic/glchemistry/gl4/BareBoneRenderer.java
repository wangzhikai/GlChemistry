/**
 * @author Zhikai Wang
 * www.heteroclinic.net
 * Please read the accompanying LICENSE
 */
package net.heteroclinic.glchemistry.gl4;

/*
 * NOTE:
 * 1. The pipeline now are only projection, transformation
 * 2. Use the Uniform stuff to share stuff main MEM and GPU
 * 
 * QUESTIONS AND TODOS
 * TO-DO 1. Remove the randomness, not necessary
 * TODO 1+1 Draw static axis
 * TODO 1+1.1 Draw a rectangle bar
 * TODO 2. Change projection matrix, from perspective to 
 * TODO 3. how reshape affects projection
 * TODO 4. How NO_OF_INSTANCE matters? 
 * vp0.replaceInShaderSource("NO_OF_INSTANCE", String.valueOf(NO_OF_INSTANCE));
 * Looks like each VBO would have a mat queued into the projection each and transform each?
 */
import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLUniformData;
import javax.media.opengl.TraceGL4;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

public class BareBoneRenderer implements GLEventListener {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BareBoneNewt(new BareBoneRenderer());
			}
		});
	}

	protected PMVMatrix projectionMatrix;
	protected GLUniformData projectionMatrixUniform;
	protected GLUniformData transformMatrixUniform;
	protected final FloatBuffer triangleTransform = FloatBuffer.allocate(16 * NO_OF_INSTANCE);

	
	
	protected static final boolean useInterleaved = true;
	protected boolean isInitialized = false;

	
	@Override
	public void init(GLAutoDrawable drawable) {
		
		GL4 gl = drawable.getGL().getGL4();
		//drawable.setGL(new DebugGL4(gl));

		gl.glClearColor(1, 1, 1, 1); //white background
//		gl.glClearColor(0, 0, 0, 1); //black background
		gl.glClearDepth(1.0f);

//		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
//		System.err.println("INIT GL IS: " + gl.getClass().getName());
//		System.err.println("GL_VENDOR: " + gl.glGetString(GL4.GL_VENDOR));
//		System.err.println("GL_RENDERER: " + gl.glGetString(GL4.GL_RENDERER));
//		System.err.println("GL_VERSION: " + gl.glGetString(GL4.GL_VERSION));

		initShader(gl);
        projectionMatrix = new PMVMatrix();
		projectionMatrixUniform = new GLUniformData("mgl_PMatrix", 4, 4, projectionMatrix.glGetPMatrixf());
		st.ownUniform(projectionMatrixUniform);
        if(!st.uniform(gl, projectionMatrixUniform)) {
            throw new GLException("Error setting mgl_PMatrix in shader: " + st);
        }

        transformMatrixUniform =  new GLUniformData("mgl_MVMatrix", 4, 4, triangleTransform);

        st.ownUniform(transformMatrixUniform);
        if(!st.uniform(gl, transformMatrixUniform)) {
            throw new GLException("Error setting mgl_MVMatrix in shader: " + st);
        }

        if(useInterleaved) {
        	initVBO_interleaved(gl);
        } else {
        	initVBO_nonInterleaved(gl);
        }

		isInitialized = true;

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
		GL4 gl = drawable.getGL().getGL4();
		st.destroy(gl);
	}

	protected IInstancedRenderingView view;
	
	public IInstancedRenderingView getView() {
		return view;
	}

	public void setView(IInstancedRenderingView view) {
		this.view = view;
	}

	public BareBoneRenderer() {
		//this.view = view;
		initTransform();
	}
	
	protected void initTransform() {
		Random rnd = new Random();
		for(int i = 0; i < NO_OF_INSTANCE; i++) {
			rotationSpeed[i] = 0.3f * rnd.nextFloat();
			mat[i] = new Matrix4();
			mat[i].loadIdentity();
			float scale = 1f + 4 * rnd.nextFloat();
			mat[i].scale(scale, scale, scale);
			//setup initial position of each triangle
			//mat[i].translate(20f * rnd.nextFloat() - 10f,10f * rnd.nextFloat() -  5f,0f);
		}
	}
	
	protected void generateTriangleTransform() {
		triangleTransform.clear();
		for(int i = 0; i < NO_OF_INSTANCE; i++) {
			mat[i].rotate(rotationSpeed[i], 0, 0, 1);
			triangleTransform.put(mat[i].getMatrix());
		}
		triangleTransform.rewind();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		if(!isInitialized ) return;

		GL4 gl = drawable.getGL().getGL4();
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

		st.useProgram(gl, true);
		projectionMatrix.glMatrixMode(GL2.GL_PROJECTION);
		projectionMatrix.glPushMatrix();

		float winScale = 1.0f;
		if(view != null) winScale = view.getScale();
		projectionMatrix.glScalef(winScale, winScale, winScale);
		projectionMatrix.update();
		st.uniform(gl, projectionMatrixUniform);
		projectionMatrix.glPopMatrix();

		generateTriangleTransform();
		
		st.uniform(gl, transformMatrixUniform);
		if(useInterleaved) {
			interleavedVBO.enableBuffer(gl, true);
		} else {
			verticesVBO.enableBuffer(gl, true);
			colorsVBO.enableBuffer(gl, true);
		}
		//gl.glVertexAttribDivisor() is not required since each instance has the same attribute (color).
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLES, 0, 3, NO_OF_INSTANCE);
		if(useInterleaved) {
			interleavedVBO.enableBuffer(gl, false);
		} else {
			verticesVBO.enableBuffer(gl, false);
			colorsVBO.enableBuffer(gl, false);
		}
		//UnlightedAxis.draw(gl);
		st.useProgram(gl, false); 
		
		/* An old GL2 Implementation
		GL2 gl = drawable.getGL().getGL2();
		if (GLProfile.isAWTAvailable()
				&& (drawable instanceof javax.media.opengl.awt.GLJPanel)
				&& !((javax.media.opengl.awt.GLJPanel) drawable).isOpaque()
				&& ((javax.media.opengl.awt.GLJPanel) drawable)
						.shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}

		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		gl.glCullFace(GL.GL_FRONT);
		gl.glFrontFace(GL.GL_CCW);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		this.glcamera.applyProjection(gl, glu);
		// gl.glOrtho(-orthoganal_clip_size, orthoganal_clip_size,
		// -orthoganal_clip_size2, orthoganal_clip_size2, NEAR_Z, FAR_Z);
		gl.glScalef(scale, scale, scale);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		// glu.gluLookAt(eyex, eyey, eyez, lookatx, lookaty, lookatz, 0, 1, 0);

		this.glcamera.applyLookat(glu);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		
		// //test case I
		// //DrawableCollection.getInstance().draw(drawable);
		// MathVector3 v = new MathVector3(0f,1f,0f);
		// System.out.println("v:"+v);
		// MathVector3 w = new MathVector3(1f,0f,0f);
		// System.out.println("w:"+w);
		// System.out.println("w':"+
		// GLOrientation.rotateAboutAVector(drawable,
		// v, w, 90f)
		// );
		// //test case I
		UnlightedAxis.draw(gl);
		*/
		
	}
	
	protected float aspect;


	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL4 gl3 = drawable.getGL().getGL4();
		gl3.glViewport(0, 0, width, height);
		aspect = (float) width / (float) height;

		projectionMatrix.glMatrixMode(GL2.GL_PROJECTION);
		projectionMatrix.glLoadIdentity();
		projectionMatrix.gluPerspective(45, aspect, 0.001f, 20f);
		projectionMatrix.gluLookAt(0, 0, -10, 0, 0, 0, 0, 1, 0);
	}
	
	protected GLArrayDataServer interleavedVBO;
	protected GLArrayDataClient verticesVBO;
	protected GLArrayDataClient colorsVBO;
	
	protected static final float[] vertices = {
		1.0f, 0.0f, 0,
		-0.5f, 0.866f, 0,
		-0.5f, -0.866f, 0
	};
	
	protected final float[] colors = {
			1.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0f, 0f, 1.0f, 1f
	};
	protected void initVBO_nonInterleaved(GL4 gl) {
		int VERTEX_COUNT = 3;

        verticesVBO = GLArrayDataClient.createGLSL("mgl_Vertex", 3, GL4.GL_FLOAT, false, VERTEX_COUNT);
        FloatBuffer verticeBuf = (FloatBuffer)verticesVBO.getBuffer();
        verticeBuf.put(vertices);
        verticesVBO.seal(gl, true);

        colorsVBO = GLArrayDataClient.createGLSL("mgl_Color",  4, GL4.GL_FLOAT, false, VERTEX_COUNT);
        FloatBuffer colorBuf = (FloatBuffer)colorsVBO.getBuffer();
        colorBuf.put(colors);
        colorsVBO.seal(gl, true);

        verticesVBO.enableBuffer(gl, false);
        colorsVBO.enableBuffer(gl, false);

        st.ownAttribute(verticesVBO, true);
        st.ownAttribute(colorsVBO, true);
		st.useProgram(gl, false);
	}


	protected void initVBO_interleaved(GL4 gl) {
		int VERTEX_COUNT = 3;
		interleavedVBO = GLArrayDataServer.createGLSLInterleaved(3 + 4, GL.GL_FLOAT, false, VERTEX_COUNT, GL.GL_STATIC_DRAW);
        interleavedVBO.addGLSLSubArray("mgl_Vertex", 3, GL.GL_ARRAY_BUFFER);
        interleavedVBO.addGLSLSubArray("mgl_Color",  4, GL.GL_ARRAY_BUFFER);

        FloatBuffer ib = (FloatBuffer)interleavedVBO.getBuffer();

        for(int i = 0; i < VERTEX_COUNT; i++) {
            ib.put(vertices,  i*3, 3);
            ib.put(colors,    i*4, 4);
        }
        interleavedVBO.seal(gl, true);
        interleavedVBO.enableBuffer(gl, false);
        st.ownAttribute(interleavedVBO, true);
		st.useProgram(gl, false);
	}
	
	protected static final String shaderBasename = "triangles";
	protected ShaderState st;
	protected static final int NO_OF_INSTANCE = 1;
	//protected final FloatBuffer triangleTransform = FloatBuffer.allocate(16 * NO_OF_INSTANCE);
	protected final Matrix4[] mat = new Matrix4[NO_OF_INSTANCE];
	protected final float[] rotationSpeed = new float[NO_OF_INSTANCE];
	
	protected void initShader(GL4 gl) {
//        public static ShaderCode create(final GL2ES2 gl, final int type, final Class<?> context,
//                final String srcRoot, final String binRoot, final String basename, final boolean mutableStringBuilder) {
        ShaderCode vp0 = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(),
                "shaders", "shader/bin", shaderBasename, true);
        
        ShaderCode fp0 = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(),
                "shaders", "shader/bin", shaderBasename, true);
        vp0.replaceInShaderSource("NO_OF_INSTANCE", String.valueOf(NO_OF_INSTANCE));

        vp0.defaultShaderCustomization(gl, true, true);
        fp0.defaultShaderCustomization(gl, true, true);

        //vp0.dumpShaderSource(System.out);

        // Create & Link the shader program
        ShaderProgram sp = new ShaderProgram();
        sp.add(vp0);
        sp.add(fp0);
        if(!sp.link(gl, System.err)) {
            throw new GLException("Couldn't link program: "+sp);
        }

        // Let's manage all our states using ShaderState.
        st = new ShaderState();
        st.attachShaderProgram(gl, sp, true);
    }
/* My old GL2 camera code
	public void glinit ( GLAutoDrawable drawable) {
		if (glinited )
			return;
		else {
			MathVector3 viewvector_negative = MathVector3.minus(this.global_position,this.lookat);
			if (viewvector_negative.norm2() < safe_lookat_radius) {
				throw new IllegalArgumentException ("The lookat point is inside the safe lookat radius.");
			}
			
			if (MathConstants.isAlmost(viewvector_negative.normalizeZeroifZero(),0f)) {
				throw new IllegalArgumentException ("The lookat point and camera position overlapped.");
			}
			MathVector3 ref = MathVector3.cross(viewvector_negative, GLOrientation.y_positive);
			if (MathConstants.isAlmost(ref.normalizeZeroifZero(),0f)) {
				throw new IllegalArgumentException ("The view vector is near north pole.");
			}
			float a = MathVector3.angleBetweenVW(viewvector_negative,GLOrientation.y_positive, ref);
			//System.out.println("a in SimpleGLCamera=" + a);
			if ( a< north_pole_restricted_angle || a > (180f - north_pole_restricted_angle))
				throw new IllegalArgumentException ("The view vector is near north pole.");
			MathVector3 viewvector = MathVector3.minus(this.lookat,this.global_position);
			//this.pinByAVectorAndKeepUpOrthogonal(drawable, viewvector);
			this.pinAPointAndKeepUpOrthogonal(drawable, this.lookat);
			 updateGetLookat_radius();
			
			// Orientation change 
		}
		 glinited = true;
	}
 */

}
