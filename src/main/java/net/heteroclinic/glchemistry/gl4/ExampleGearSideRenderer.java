/**
 * @author Zhikai Wang
 * www.heteroclinic.net
 * Please read the accompanying LICENSE
 * This class is the example to draw a triangle fan.
 * TODO Draw a fan circle
 * TODO Draw side of gear
 * TODO Draw the tooth
 * TODO Upload the triangulation strategy plot
 * The gear geometry data refer to 
 * Gears.java
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Gothel)
 *
gear(gl, 1.0f, 4.0f, 1.0f, 20, 0.7f);
gear(gl, 0.5f, 2.0f, 2.0f, 10, 0.7f);
gear(gl, 1.3f, 2.0f, 0.5f, 10, 0.7f);
gear(GL2 gl,float inner_radius,float outer_radius,float width,
                   int teeth, float tooth_depth) 
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

public class ExampleGearSideRenderer extends Renderer {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BareBoneNewt(new ExampleGearSideRenderer());
			}
		});
	}
	
    //public static final int VERTEX_COUNT = 5;

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


        float radius = 16.f;
        
        if(useInterleaved) {
        	initFanCircle_VBO_interleaved(totalFans, radius,gl);
        } else {
        	initFanCircle_VBO_nonInterleaved(totalFans, radius,gl);
        }
		isInitialized = true;
		
	}
	public static final int totalFans = 36;

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

	public ExampleGearSideRenderer() {
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
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_FAN, 0, totalFans+2, NO_OF_INSTANCE);
		//gl.glVertexAttribDivisor();
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLES, 2, 4, NO_OF_INSTANCE);
		
		if(useInterleaved) {
			interleavedVBO.enableBuffer(gl, false);
		} else {
			verticesVBO.enableBuffer(gl, false);
			colorsVBO.enableBuffer(gl, false);
		}

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
	
//	public static final float realWorldScale = 4.0f;
//	protected static final float[] vertices = {
//		1.0f*realWorldScale, 0.0f*realWorldScale, 0,
//		-0.5f*realWorldScale, 0.866f*realWorldScale, 0,
//		-0.5f*realWorldScale, -0.866f*realWorldScale, 0,
//		(-1.0f-0.866f) *realWorldScale, 0f*realWorldScale, 0,
//		(-1.0f-0.866f) *realWorldScale, -0.866f*2.0f*realWorldScale, 0
//	};	
//	protected final float[] colors = {
//			1.0f, 0.0f, 0.0f, 1.0f,
//			0.0f, 1.0f, 0.0f, 1.0f,
//			0f, 0f, 1.0f, 1f,
//			0.0f, 1.0f, 0.0f, 1.0f,
//			1.0f, 0.0f, 0.0f, 1.0f
//	};
	
	final static public int vertexDimension =3 ;
	final static public int colorDimension =4 ;
	protected void initFanCircle_VBO_nonInterleaved(int VERTEX_COUNT, float r, GL4 gl) {
		{
			
			float [] vertices = new float [(VERTEX_COUNT+1) * vertexDimension];
			float step =  2.0f * (float) Math.PI / VERTEX_COUNT;
			float angle = 0f;
			for (int i = 0; i <= VERTEX_COUNT; i++) {
				vertices[0+i*vertexDimension] = 0f;
				vertices[1+i*vertexDimension] = r * (float)Math.cos(angle);
				vertices[2+i*vertexDimension] = r * (float)Math.sin(angle);
				angle += step;
			}
			System.out.println(vertices);

			float [] colors = new float [(VERTEX_COUNT+1) * colorDimension];
			for (int i = 0; i <= VERTEX_COUNT; i++) {
				colors[0+i*colorDimension] = (i%3==1)?1.0f:0f ;
				colors[1+i*colorDimension] = (i%3==2)?1.0f:0f ;
				colors[2+i*colorDimension] = (i%3==0)?1.0f:0f ;
				colors[3+i*colorDimension] = 1.0f;
			}

			
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
	}


	protected void initFanCircle_VBO_interleaved(int fans,float r,GL4 gl) {
		{
			int vertices_per_fan = 4; 
		
			//float [] vertices = new float [(fans+2) * vertexDimension];
			float [] vertices = new float [(fans*vertices_per_fan +1) * vertexDimension];
			float step =  2.0f * (float) Math.PI / fans;
			float da = step/4.0f;
			float theta = 0f;
			vertices[0+0*vertexDimension] = 0f;
			vertices[2+0*vertexDimension] =  0f;
			vertices[1+0*vertexDimension] =  0f;
			
			
			//float radius = 16.f;
			float toothDepth = 2.8f;
			float rin = r - toothDepth/2.0f;
			float rout = r + toothDepth/2.0f;
			
			for (int i = 0; i< fans ; i++) {
				// point 1
				vertices[3+ 0*3+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta) ;
				vertices[3+ 0*3+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta);
				vertices[3+ 0*3+i*vertices_per_fan*vertexDimension +2] = 0f;

				// point 2
				vertices[3+ 1*3+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+da) ;
				vertices[3+ 1*3+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+da);
				vertices[3+ 1*3+i*vertices_per_fan*vertexDimension +2] = 0f;

				// point 2
				vertices[3+ 2*3+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+2.0f*da) ;
				vertices[3+ 2*3+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+2.0f*da);
				vertices[3+ 2*3+i*vertices_per_fan*vertexDimension +2] = 0f;

				// point 2
				vertices[3+ 3*3+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+3.0f*da) ;
				vertices[3+ 3*3+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+3.0f*da);
				vertices[3+ 3*3+i*vertices_per_fan*vertexDimension +2] = 0f;

				
				theta += step;
			}

			
			for (int i =2; i  <=fans+2; i++) {
				
				vertices[0+(i-1)*vertexDimension] =r * (float)Math.cos(theta);
				vertices[1+(i-1)*vertexDimension] = r * (float)Math.sin(theta);
				vertices[2+(i-1)*vertexDimension] = 0f;
				theta += step;
				
			}
			for (int i = 0; i<vertices.length; i++) {
				if (i%3 == 0)
					System.out.println();
				System.out.print(vertices[i]+ " ");
			}
			
			float [] colors = new float [(fans*vertices_per_fan +1) * colorDimension];
			for (int i = 0; i <= fans+1; i++) {
				colors[0+i*colorDimension] = (i%3==1)?1.0f:0f ;
				colors[1+i*colorDimension] = (i%3==2)?1.0f:0f ;
				colors[2+i*colorDimension] = (i%3==0)?1.0f:0f ;
				colors[3+i*colorDimension] = 1.0f;
			}
			interleavedVBO = GLArrayDataServer.createGLSLInterleaved(3 + 4, GL.GL_FLOAT, false, fans+2, GL.GL_STATIC_DRAW);
	        interleavedVBO.addGLSLSubArray("mgl_Vertex", 3, GL.GL_ARRAY_BUFFER);
	        interleavedVBO.addGLSLSubArray("mgl_Color",  4, GL.GL_ARRAY_BUFFER);
	
	        FloatBuffer ib = (FloatBuffer)interleavedVBO.getBuffer();
	
	        for(int i = 0; i < fans+2; i++) {
	            ib.put(vertices,  i*3, 3);
	            ib.put(colors,    i*4, 4);
	        }
	        interleavedVBO.seal(gl, true);
	        interleavedVBO.enableBuffer(gl, false);
	        st.ownAttribute(interleavedVBO, true);
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
