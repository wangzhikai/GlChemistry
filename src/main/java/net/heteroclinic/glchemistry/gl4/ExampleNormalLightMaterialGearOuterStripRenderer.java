/**
 * @author Zhikai Wang
 * www.heteroclinic.net
 * Please read the accompanying LICENSE
 * 
 * I happened to design a gear box in my first engineering degree.
 * The whole process took about two months, hand draft pencil paper calculator work. 
 * AUTOCAD was expensive and not allowed. 
 * The gear teeth don't have flat surface, they are of smooth curve so the line of contact is always maintained.
 * Otherwise, the gear box will clank horribly. My design was never put to real, but the professor reviewed it seriously.
 * For the beauty, my gears are like two twins (just one shift). The two gears sizes are quite even -- 1:1.x something.
 * But the professor said there is no meaning to design such a gear box. 
 * At least it should be 1:2.x, 1:3 better. This is something you never do you never know.
 *  
 * Task list
 * TO-DO Draw a fan circle
 * TO-DO Draw side of gear
 * TO-DO Draw the tooth
 * TO-DO Draw the gear with shaft hole
 * TODO Upload the triangulation strategy plot
 * 
 * TO-DO Light
 * TO-DO Material
 * TODO Compute normal
 * TODO Light-material shaders
 * TODO Need check bunny's shaders.
 * 
 * References:
1.The gear geometry data refer to 
 * Gears.java
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Gothel)
 *
gear(gl, 1.0f, 4.0f, 1.0f, 20, 0.7f);
gear(gl, 0.5f, 2.0f, 2.0f, 10, 0.7f);
gear(gl, 1.3f, 2.0f, 0.5f, 10, 0.7f);
gear(GL2 gl,float inner_radius,float outer_radius,float width,
                   int teeth, float tooth_depth) 
                   
2. http://www.opengl-redbook.com/ /04/ch04_shadowmap/
3. OpenGL Programming Guide Eighth Edition  -- Dave Shreiner, Graham Sellers, John Kessenich, Bill Licea-kane
 * 
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

public class ExampleNormalLightMaterialGearOuterStripRenderer extends Renderer {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BareBoneNewt(new ExampleNormalLightMaterialGearOuterStripRenderer());
			}
		});
	}
	
    //public static final int VERTEX_COUNT = 5;

	protected PMVMatrix projectionMatrix;
	protected GLUniformData projectionMatrixUniform;
	protected GLUniformData transformMatrixUniform;
	//protected GLUniformData lightPositionUniform;
	protected final FloatBuffer triangleTransform = FloatBuffer.allocate(16 * NO_OF_INSTANCE);
	protected final FloatBuffer lightPosition = FloatBuffer.allocate(3);
	protected int lightPositionLocation = -1;

	protected final FloatBuffer dummyBuffer = FloatBuffer.allocate(4);
	protected int dummyLocation = -1;

	protected final FloatBuffer material_ambientBuffer = FloatBuffer.allocate(3);
	protected int material_ambientLocation = -1;
	protected final FloatBuffer material_diffuseBuffer = FloatBuffer.allocate(3);
	protected int material_diffuseLocation = -1;
	protected final FloatBuffer material_specularBuffer = FloatBuffer.allocate(3);
	protected int material_specularLocation = -1;
	protected final FloatBuffer material_specular_powerBuffer = FloatBuffer.allocate(1);
	protected int material_specular_powerLocation = -1;
	protected final FloatBuffer eye_coordBuffer = FloatBuffer.allocate(3);
	protected int eye_coordLocation = -1;
	
	//uniform vec3 eye_coord;
	/*
	uniform vec3 material_ambient;
	uniform vec3 material_diffuse;
	uniform vec3 material_specular;
	uniform float material_specular_power;
	*/
	
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
		//gl.glGetUniformLocation(render_scene_prog, "light_position");
		int shaderProgramId = initShader(gl);
        projectionMatrix = new PMVMatrix();
		projectionMatrixUniform = new GLUniformData("mgl_PMatrix", 4, 4, projectionMatrix.glGetPMatrixf());
		st.ownUniform(projectionMatrixUniform);
        if(!st.uniform(gl, projectionMatrixUniform)) {
            throw new GLException("Error setting mgl_PMatrix in shader: " + st);
        }

        transformMatrixUniform = new GLUniformData("mgl_MVMatrix", 4, 4, triangleTransform);
        st.ownUniform(transformMatrixUniform);
        if(!st.uniform(gl, transformMatrixUniform)) {
            throw new GLException("Error setting mgl_MVMatrix in shader: " + st);
        }
        //TODO
        lightPositionLocation = gl.glGetUniformLocation(shaderProgramId, "light_position");
        dummyLocation = gl.glGetUniformLocation(shaderProgramId, "dummy");
        
        material_ambientLocation = gl.glGetUniformLocation(shaderProgramId, "material_ambient");
        material_diffuseLocation = gl.glGetUniformLocation(shaderProgramId, "material_diffuse");
        material_specularLocation = gl.glGetUniformLocation(shaderProgramId, "material_specular");
        material_specular_powerLocation = gl.glGetUniformLocation(shaderProgramId, "material_specular_power");
        eye_coordLocation = gl.glGetUniformLocation(shaderProgramId, "eye_coord");
//    	protected final FloatBuffer eye_coordBuffer = FloatBuffer.allocate(3);
//    	protected int eye_coordLocation = -1;
//    	protected final FloatBuffer material_ambientBuffer = FloatBuffer.allocate(3);
//    	protected int material_ambientLocation = -1;
//    	protected final FloatBuffer material_diffuseBuffer = FloatBuffer.allocate(3);
//    	protected int material_diffuseLocation = -1;
//    	protected final FloatBuffer material_specularBuffer = FloatBuffer.allocate(3);
//    	protected int material_specularLocation = -1;
//    	protected final FloatBuffer material_specular_powerBuffer = FloatBuffer.allocate(1);
//    	protected int material_specular_powerLocation = -1;
//    	/*
//    	uniform vec3 material_ambient;
//    	uniform vec3 material_diffuse;
//    	uniform vec3 material_specular;
//    	uniform float material_specular_power;
//    	*/
        

        /* This block fails the program
        // light positioned at (10.f,10.f,10.f)
//        lightPosition.clear();
//        lightPosition.put(new float[] {10.f,10.f,10.f,1f});
//        lightPosition.rewind();
        lightPositionUniform =  new GLUniformData("light_position", 4, lightPosition);

        st.ownUniform(lightPositionUniform);
        if(!st.uniform(gl, lightPositionUniform)) { //failed here
            throw new GLException("Error setting lightPosition in shader: " + st);
        }
        */

        float radius = 16.f;
        float shaft_radius = 4.0f;
        
        if(useInterleaved) {
        	initFanCircle_VBO_interleaved(totalFans,radius,shaft_radius, gl);
        } else {
        	initFanCircle_VBO_nonInterleaved(totalFans, radius,gl);
        }
		isInitialized = true;
		
	}
	public static final int totalFans = 20;

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

	public ExampleNormalLightMaterialGearOuterStripRenderer() {
		//this.view = view;
		initTransform();
	}//			for (int i = 0; i<vertices.length; i++) {
//	if (i % (vertices_per_fan*vertexDimension) == 0)
//	System.out.println();
//
//if (i%3 == 0)
//	System.out.println();
//System.out.print(vertices[i]+ " ");
//}

	
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
		
		//public void glUniform3fv(int location, int count, FloatBuffer v);
        //light positioned at (10.f,10.f,10.f)
      lightPosition.clear();
      lightPosition.put(new float[] {10.f,10.f,10.f});
      lightPosition.rewind();
		gl.glUniform3fv(lightPositionLocation, 1, lightPosition);

		
	      dummyBuffer.clear();
	      dummyBuffer.put(new float[] {0.f,0.f,1.f,1f});
	      dummyBuffer.rewind();
	      gl.glUniform4fv(dummyLocation, 1, dummyBuffer);

			
	      material_ambientBuffer.clear();
	      material_ambientBuffer.put(new float[] {0.1f,0.f,0.2f});
	      material_ambientBuffer.rewind();
	      gl.glUniform3fv(material_ambientLocation, 1, material_ambientBuffer);

	      material_diffuseBuffer.clear();
	      material_diffuseBuffer.put(new float[] {0.3f,0.2f,0.8f});
	      material_diffuseBuffer.rewind();
	      gl.glUniform3fv(material_diffuseLocation, 1, material_diffuseBuffer);

	      material_specularBuffer.clear();
	      material_specularBuffer.put(new float[] {1f,1f,1f});
	      material_specularBuffer.rewind();
	      gl.glUniform3fv(material_specularLocation, 1,material_specularBuffer);

	      material_specular_powerBuffer.clear();
	      material_specular_powerBuffer.put(new float[] {3.0f});
	      material_specular_powerBuffer.rewind();
	      gl.glUniform1fv(material_specular_powerLocation, 1, material_specular_powerBuffer);
	      
	      eye_coordBuffer.clear();
	      eye_coordBuffer.put(eyePostion);
	      eye_coordBuffer.rewind();
	      gl.glUniform3fv(eye_coordLocation, 1, eye_coordBuffer);

	      
//	  	protected final FloatBuffer eye_coordBuffer = FloatBuffer.allocate(3);
//		protected int eye_coordLocation = -1;


//	    	protected final FloatBuffer material_specularBuffer = FloatBuffer.allocate(3);
//	    	protected int material_specularLocation = -1;
//	    	protected final FloatBuffer material_specular_powerBuffer = FloatBuffer.allocate(1);
//	    	protected int material_specular_powerLocation = -1;
//	    	/*
//	    	uniform vec3 material_ambient;
//	    	uniform vec3 material_diffuse;
//	    	uniform vec3 material_specular;
//	    	uniform float material_specular_power;
//	    	*/
			
		
		/* TODO CULL_FACE causes some problem, need fix, probably CCW issue in traingulation. 
		gl.glEnable(GL4.GL_CULL_FACE);
		gl.glEnable(GL4.GL_DEPTH_TEST);
		gl.glDepthFunc(GL4.GL_LEQUAL);		
*/
		/* TODO Investigate
   // Enable polygon offset to resolve depth-fighting isuses
    glEnable(GL_POLYGON_OFFSET_FILL);
    glPolygonOffset(2.0f, 4.0f);
    // Draw from the light's point of view
    DrawScene(true);
    glDisable(GL_POLYGON_OFFSET_FILL);
		 */
		
		/*TODO-1 MATERIAL
    // Set material properties for the object
    if (!depth_only)
    {
        glUniform3fv(render_scene_uniforms.material_ambient, 1, vec3(0.1f, 0.0f, 0.2f));
        glUniform3fv(render_scene_uniforms.material_diffuse, 1, vec3(0.3f, 0.2f, 0.8f));
        glUniform3fv(render_scene_uniforms.material_specular, 1, vec3(1.0f, 1.0f, 1.0f));
        glUniform1f(render_scene_uniforms.material_specular_power, 25.0f);
    }
    
        struct
    {
        GLint model_matrix;
        GLint view_matrix;
        GLint projection_matrix;
        GLint shadow_matrix;
        GLint light_position;
        GLint material_ambient;
        GLint material_diffuse;
        GLint material_specular;
        GLint material_specular_power;
    } render_scene_uniforms;
    
    // Get the locations of all the uniforms in the program
    render_scene_uniforms.model_matrix = glGetUniformLocation(render_scene_prog, "model_matrix");
    render_scene_uniforms.view_matrix = glGetUniformLocation(render_scene_prog, "view_matrix");
    render_scene_uniforms.projection_matrix = glGetUniformLocation(render_scene_prog, "projection_matrix");
    render_scene_uniforms.shadow_matrix = glGetUniformLocation(render_scene_prog, "shadow_matrix");
    render_scene_uniforms.light_position = glGetUniformLocation(render_scene_prog, "light_position");
    render_scene_uniforms.material_ambient = glGetUniformLocation(render_scene_prog, "material_ambient");
    render_scene_uniforms.material_diffuse = glGetUniformLocation(render_scene_prog, "material_diffuse");
    render_scene_uniforms.material_specular = glGetUniformLocation(render_scene_prog, "material_specular");
    render_scene_uniforms.material_specular_power = glGetUniformLocation(render_scene_prog, "material_specular_power");
		 */
		
		/* TODO-3 ? Normal
		 * 
		 */

		
		if(useInterleaved) {
			interleavedVBO.enableBuffer(gl, true);
		} else {
			verticesVBO.enableBuffer(gl, true);
			colorsVBO.enableBuffer(gl, true);
		}
		//gl.glVertexAttribDivisor() is not required since each instance has the same attribute (color).
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLES, 0, 3, NO_OF_INSTANCE);
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 5, NO_OF_INSTANCE);
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_FAN, 0, totalFans+2, NO_OF_INSTANCE);
		int totalVertices = totalFans*vertices_per_fan +2 ;
		//gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_FAN, 0, totalVertices, NO_OF_INSTANCE);
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, totalVertices, NO_OF_INSTANCE);
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
		
//	    public final void gluLookAt(final float eyex, final float eyey, final float eyez,
//                final float centerx, final float centery, final float centerz,
//                final float upx, final float upy, final float upz) {
		projectionMatrix.gluLookAt(eyePostion[0], eyePostion[1], eyePostion[2], 0, 0, 0, 0, 1, 0);
	}
	protected float [] eyePostion = new float [] {0, 8, 8};
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
	final static public int normalDimension =3 ;
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

	public static final int vertices_per_fan = 8; 

	protected void initFanCircle_VBO_interleaved(int fans,float r,float s ,GL4 gl) {
		{
			float width = 2.0f;
			//int vertices_per_fan = 4; 
		
			//float [] vertices = new float [(fans+2) * vertexDimension];
			//int totalVertices = (fans*vertices_per_fan +2);
			int totalVertices = (fans*vertices_per_fan +2 );
			float [] vertices = new float [totalVertices * vertexDimension];
			float step =  2.0f * (float) Math.PI / fans;
			float da = step/4.0f;
			float theta = 0f;
//			vertices[0+0*vertexDimension] = 0f;
//			vertices[2+0*vertexDimension] =  0f;
//			vertices[1+0*vertexDimension] =  0f;
			
			
			//float radius = 16.f;
			float toothDepth = 2.8f;
			float rin = r - toothDepth/2.0f;
			float rout = r + toothDepth/2.0f;
			
			for (int i = 0; i< fans ; i++) {
				if (i == 0) {
					// Note: alternate 2 and 1 to follow CCW (counter-clock-wise) rule
					// point 2
					vertices[0*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta) ;
					vertices[0*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta);
					vertices[0*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
	
					// point 1
					vertices[1*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta) ;
					vertices[1*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta);
					vertices[1*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
					
					// point 3
					vertices[2*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+da) ;
					vertices[2*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+da);
					vertices[2*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
	
					// point 4
					vertices[3*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+da) ;
					vertices[3*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+da);
					vertices[3*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
	
					// point 5
					vertices[4*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+2.0f*da) ;
					vertices[4*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+2.0f*da);
					vertices[4*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
					
					// point 6
					vertices[5*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+2.0f*da) ;
					vertices[5*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+2.0f*da);
					vertices[5*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
	
					// point 7
					vertices[6*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+3.0f*da) ;
					vertices[6*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+3.0f*da);
					vertices[6*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
					
					// point 8
					vertices[7*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+3.0f*da) ;
					vertices[7*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+3.0f*da);
					vertices[7*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
					
					// point 9
					vertices[8*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+4.0f*da) ;
					vertices[8*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+4.0f*da);
					vertices[8*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
	
					// point 10
	
					vertices[9*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+4.0f*da) ;
					vertices[9*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+4.0f*da);
					vertices[9*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
				} else {
					// point 3
					vertices[2*vertexDimension + 0*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+da) ;
					vertices[2*vertexDimension + 0*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+da);
					vertices[2*vertexDimension + 0*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
	
					// point 4
					vertices[2*vertexDimension + 1*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+da) ;
					vertices[2*vertexDimension + 1*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+da);
					vertices[2*vertexDimension + 1*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
	
					// point 5
					vertices[2*vertexDimension + 2*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+2.0f*da) ;
					vertices[2*vertexDimension + 2*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+2.0f*da);
					vertices[2*vertexDimension + 2*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
					
					// point 6
					vertices[2*vertexDimension + 3*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rout *  (float)Math.cos(theta+2.0f*da) ;
					vertices[2*vertexDimension + 3*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rout *  (float)Math.sin(theta+2.0f*da);
					vertices[2*vertexDimension + 3*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
	
					// point 7
					vertices[2*vertexDimension + 4*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+3.0f*da) ;
					vertices[2*vertexDimension + 4*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+3.0f*da);
					vertices[2*vertexDimension + 4*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
					
					// point 8
					vertices[2*vertexDimension + 5*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+3.0f*da) ;
					vertices[2*vertexDimension + 5*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+3.0f*da);
					vertices[2*vertexDimension + 5*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
					
					// point 9
					vertices[2*vertexDimension + 6*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+4.0f*da) ;
					vertices[2*vertexDimension + 6*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+4.0f*da);
					vertices[2*vertexDimension + 6*vertexDimension+i*vertices_per_fan*vertexDimension +2] = -width/2.0f;
	
					// point 10
	
					vertices[2*vertexDimension + 7*vertexDimension+i*vertices_per_fan*vertexDimension +0] = rin *  (float)Math.cos(theta+4.0f*da) ;
					vertices[2*vertexDimension + 7*vertexDimension+i*vertices_per_fan*vertexDimension +1] = rin *  (float)Math.sin(theta+4.0f*da);
					vertices[2*vertexDimension + 7*vertexDimension+i*vertices_per_fan*vertexDimension +2] = width/2.0f;
					
				}
				
				theta += step;
			}
			
//			for (int i = 0; i<vertices.length; i++) {
//				if (i % (vertices_per_fan*vertexDimension) == 0)
//					System.out.println();
//
//				if (i%3 == 0)
//					System.out.println();
//				System.out.print(vertices[i]+ " ");
//			}
			
			float [] normals = new float [totalVertices * normalDimension];
			theta = 0f;
			step = ((float)Math.PI )*2.f / totalVertices;
					
			for (int i = 0; i < totalVertices; i++) {
				normals[0+i*normalDimension] =   (float)Math.cos(theta);
				normals[1+i*normalDimension] =  (float)Math.sin(theta);
				normals[2+i*normalDimension] = 0.0f;
				//normals[3+i*normalDimension] = 1.0f;
				theta += step;
			}
			interleavedVBO = GLArrayDataServer.createGLSLInterleaved(3 + 3, GL.GL_FLOAT, false, totalVertices, GL.GL_STATIC_DRAW);
	        interleavedVBO.addGLSLSubArray("mgl_Vertex", 3, GL.GL_ARRAY_BUFFER);
	        interleavedVBO.addGLSLSubArray("mgl_Normal",  3, GL.GL_ARRAY_BUFFER);
	
	        FloatBuffer ib = (FloatBuffer)interleavedVBO.getBuffer();
	
	        for(int i = 0; i < totalVertices; i++) {
	            ib.put(vertices,  i*3, 3);
	            ib.put(normals,    i*3, 3);
	        }
	        interleavedVBO.seal(gl, true);
	        interleavedVBO.enableBuffer(gl, false);
	        st.ownAttribute(interleavedVBO, true);
			st.useProgram(gl, false);
		}
	}
	
	protected static final String shaderBasename = "normalLightedTriangles";
	protected ShaderState st;
	protected static final int NO_OF_INSTANCE = 1;
	//protected final FloatBuffer triangleTransform = FloatBuffer.allocate(16 * NO_OF_INSTANCE);
	protected final Matrix4[] mat = new Matrix4[NO_OF_INSTANCE];
	protected final float[] rotationSpeed = new float[NO_OF_INSTANCE];
	
	protected int initShader(GL4 gl) {
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
        return sp.id();
    }
	
}
