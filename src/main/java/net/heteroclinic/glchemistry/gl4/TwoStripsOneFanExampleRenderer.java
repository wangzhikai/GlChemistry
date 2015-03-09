/**
 * @author Zhikai Wang
 * www.heteroclinic.net
 * Please read the accompanying LICENSE
 * This class is the example to draw a triangle fan.
 */
package net.heteroclinic.glchemistry.gl4;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLException;
import javax.media.opengl.GLUniformData;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

public class TwoStripsOneFanExampleRenderer extends Renderer {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BareBoneNewt(new TwoStripsOneFanExampleRenderer());
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

	public TwoStripsOneFanExampleRenderer() {
		//this.view = view;
		initTransform();
	}
	
	protected void initTransform() {
		Random rnd = new Random();
		for(int i = 0; i < NO_OF_INSTANCE; i++) {
			rotationSpeed[i] = 0.3f * rnd.nextFloat();
			mat[i] = new Matrix4();
			mat[i].loadIdentity();
			//float scale = 1f + 4 * rnd.nextFloat();
			float scale = 1f ;
			// TODO scale is a bad idea, it deforms the normals of a plane. 
			mat[i].scale(scale, scale, scale);
			//setup initial position of each triangle
			//mat[i].translate(20f * rnd.nextFloat() - 10f,10f * rnd.nextFloat() -  5f,0f);
		}
	}
	
	protected void generateTriangleTransform() {
		triangleTransform.clear();
		for(int i = 0; i < NO_OF_INSTANCE; i++) {
			//mat[i].rotate(rotationSpeed[i], 0, 0, 1);
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
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLES, 0, 3, NO_OF_INSTANCE);
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 5, NO_OF_INSTANCE);
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 5, NO_OF_INSTANCE);
		//gl.glVertexAttribDivisor();
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLES, 2, 4, NO_OF_INSTANCE);
		
		if(useInterleaved) {
			interleavedVBO.enableBuffer(gl, false);
		} else {
			verticesVBO.enableBuffer(gl, false);
			colorsVBO.enableBuffer(gl, false);
		}

		// DONE Try to draw the second VBO
		st.uniform(gl, transformMatrixUniform);
		if(useInterleaved) {
			interleavedVBO_2.enableBuffer(gl, true);
		} else {
			verticesVBO_2.enableBuffer(gl, true);
			colorsVBO_2.enableBuffer(gl, true);
		}
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 5, NO_OF_INSTANCE);
		if(useInterleaved) {
			interleavedVBO_2.enableBuffer(gl, false);
		} else {
			verticesVBO_2.enableBuffer(gl, false);
			colorsVBO_2.enableBuffer(gl, false);
		}
		// END Try to draw the second VBO
		
		
		// DONE Try to draw the third VBO for a GL4.GL_TRIANGLE_FAN 
		st.uniform(gl, transformMatrixUniform);
		if(useInterleaved) {
			interleavedVBO_3.enableBuffer(gl, true);
		} else {
			verticesVBO_3.enableBuffer(gl, true);
			colorsVBO_3.enableBuffer(gl, true);
		}
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_FAN, 0, 5, NO_OF_INSTANCE);
		if(useInterleaved) {
			interleavedVBO_3.enableBuffer(gl, false);
		} else {
			verticesVBO_3.enableBuffer(gl, false);
			colorsVBO_3.enableBuffer(gl, false);
		}
		// END Try to draw the third VBO

		st.useProgram(gl, false); 
		
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

	protected GLArrayDataServer interleavedVBO_2;
	protected GLArrayDataClient verticesVBO_2;
	protected GLArrayDataClient colorsVBO_2;

	protected GLArrayDataServer interleavedVBO_3;
	protected GLArrayDataClient verticesVBO_3;
	protected GLArrayDataClient colorsVBO_3;

	
	public static final float realWorldScale = 4.0f;
	protected static final float[] vertices = {
		1.0f*realWorldScale, 0.0f*realWorldScale, 0,
		-0.5f*realWorldScale, 0.866f*realWorldScale, 0,
		-0.5f*realWorldScale, -0.866f*realWorldScale, 0,
		(-1.0f-0.866f) *realWorldScale, 0f*realWorldScale, 0,
		(-1.0f-0.866f) *realWorldScale, -0.866f*2.0f*realWorldScale, 0
	};

	public static final float secondStripOffsetx =8.0f;
	public static final float secondStripOffsety = 4.0f;
	protected static final float[] vertices_2 = {
		1.0f*realWorldScale + secondStripOffsetx, 0.0f*realWorldScale + secondStripOffsety, 0,
		-0.5f*realWorldScale  + secondStripOffsetx, 0.866f*realWorldScale + secondStripOffsety, 0,
		-0.5f*realWorldScale  + secondStripOffsetx, -0.866f*realWorldScale + secondStripOffsety, 0,
		(-1.0f-0.866f) *realWorldScale + secondStripOffsetx, 0f*realWorldScale + secondStripOffsety, 0,
		(-1.0f-0.866f) *realWorldScale + secondStripOffsetx, -0.866f*2.0f*realWorldScale + secondStripOffsety, 0
	};

	protected static final float[] vertices_3 = {
		0f,4.0f,0f,
		-6f,8.0f,0f,
		-4f,10.0f,0f,
		4f,10.0f,0f,
		6f,8.0f,0f
	};
	
	protected final float[] colors = {
			1.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0f, 0f, 1.0f, 1f,
			0.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f
	};
	
	// We can use one colors, but this is low coupling.
	protected final float[] colors_2 = {
			1.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0f, 0f, 1.0f, 1f,
			0.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f
	};

	protected final float[] colors_3 = {
			1.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0f, 0f, 1.0f, 1f,
			0.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f
	};

	protected void initVBO_nonInterleaved(GL4 gl) {
		int VERTEX_COUNT = 5;
		{
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
		{
	        verticesVBO_2 = GLArrayDataClient.createGLSL("mgl_Vertex", 3, GL4.GL_FLOAT, false, VERTEX_COUNT);
	        FloatBuffer verticeBuf = (FloatBuffer)verticesVBO_2.getBuffer();
	        verticeBuf.put(vertices_2);
	        verticesVBO_2.seal(gl, true);
	
	        colorsVBO_2 = GLArrayDataClient.createGLSL("mgl_Color",  4, GL4.GL_FLOAT, false, VERTEX_COUNT);
	        FloatBuffer colorBuf = (FloatBuffer)colorsVBO_2.getBuffer();
	        colorBuf.put(colors_2);
	        colorsVBO_2.seal(gl, true);
	
	        verticesVBO_2.enableBuffer(gl, false);
	        colorsVBO_2.enableBuffer(gl, false);
	
	        st.ownAttribute(verticesVBO_2, true);
	        st.ownAttribute(colorsVBO_2, true);
			st.useProgram(gl, false);
		}		
		{
	        verticesVBO_3 = GLArrayDataClient.createGLSL("mgl_Vertex", 3, GL4.GL_FLOAT, false, VERTEX_COUNT);
	        FloatBuffer verticeBuf = (FloatBuffer)verticesVBO_3.getBuffer();
	        verticeBuf.put(vertices_3);
	        verticesVBO_3.seal(gl, true);
	
	        colorsVBO_3 = GLArrayDataClient.createGLSL("mgl_Color",  4, GL4.GL_FLOAT, false, VERTEX_COUNT);
	        FloatBuffer colorBuf = (FloatBuffer)colorsVBO_3.getBuffer();
	        colorBuf.put(colors_3);
	        colorsVBO_3.seal(gl, true);
	
	        verticesVBO_3.enableBuffer(gl, false);
	        colorsVBO_3.enableBuffer(gl, false);
	
	        st.ownAttribute(verticesVBO_3, true);
	        st.ownAttribute(colorsVBO_3, true);
			st.useProgram(gl, false);
		}
	}


	protected void initVBO_interleaved(GL4 gl) {
		int VERTEX_COUNT = 5;
		{
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
		{
			interleavedVBO_2 = GLArrayDataServer.createGLSLInterleaved(3 + 4, GL.GL_FLOAT, false, VERTEX_COUNT, GL.GL_STATIC_DRAW);
	        interleavedVBO_2.addGLSLSubArray("mgl_Vertex", 3, GL.GL_ARRAY_BUFFER);
	        interleavedVBO_2.addGLSLSubArray("mgl_Color",  4, GL.GL_ARRAY_BUFFER);
	
	        FloatBuffer ib = (FloatBuffer)interleavedVBO_2.getBuffer();
	
	        for(int i = 0; i < VERTEX_COUNT; i++) {
	            ib.put(vertices_2,  i*3, 3);
	            ib.put(colors_2,    i*4, 4);
	        }
	        interleavedVBO_2.seal(gl, true);
	        interleavedVBO_2.enableBuffer(gl, false);
	        st.ownAttribute(interleavedVBO_2, true);
			st.useProgram(gl, false);
		}
		{
			interleavedVBO_3 = GLArrayDataServer.createGLSLInterleaved(3 + 4, GL.GL_FLOAT, false, VERTEX_COUNT, GL.GL_STATIC_DRAW);
	        interleavedVBO_3.addGLSLSubArray("mgl_Vertex", 3, GL.GL_ARRAY_BUFFER);
	        interleavedVBO_3.addGLSLSubArray("mgl_Color",  4, GL.GL_ARRAY_BUFFER);
	
	        FloatBuffer ib = (FloatBuffer)interleavedVBO_3.getBuffer();
	
	        for(int i = 0; i < VERTEX_COUNT; i++) {
	            ib.put(vertices_3,  i*3, 3);
	            ib.put(colors_3,    i*4, 4);
	        }
	        interleavedVBO_3.seal(gl, true);
	        interleavedVBO_3.enableBuffer(gl, false);
	        st.ownAttribute(interleavedVBO_3, true);
			st.useProgram(gl, false);
		}
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
	
}
