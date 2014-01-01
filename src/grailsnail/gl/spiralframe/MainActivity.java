package grailsnail.gl.spiralframe;

//gles20, glutils, matrix.*

import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

    import android.app.*;
    import android.os.*;
    import android.view.*;
    import android.widget.*;

    import java.util.Random;
	import android.util.Log;
	import java.util.ArrayList;
	
	import android.content.Context;
	import java.io.InputStreamReader;
	import java.io.BufferedReader;
	import java.io.IOException;
	import java.lang.RuntimeException;
	import android.content.res.Resources.NotFoundException;

	import java.io.InputStream;
	import java.nio.FloatBuffer;
	import java.nio.ShortBuffer;
	import java.nio.ByteBuffer;
	import java.nio.ByteOrder;

    import android.opengl.GLSurfaceView;
	import android.opengl.GLSurfaceView.Renderer;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.BitmapFactory.Options;
	
	import static android.opengl.GLUtils.texImage2D;
	import javax.microedition.khronos.egl.EGLConfig;
	import javax.microedition.khronos.opengles.GL10;

	import static android.opengl.GLES20.GL_VERTEX_SHADER;
	import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
	import static android.opengl.GLES20.glCreateShader;
	import static android.opengl.GLES20.glShaderSource;
	import static android.opengl.GLES20.glCompileShader;
	import static android.opengl.GLES20.glAttachShader;

	import static android.opengl.GLES20.glCreateProgram;
	import static android.opengl.GLES20.glLinkProgram;
	import static android.opengl.GLES20.glUseProgram;
	import static android.opengl.GLES20.glValidateProgram;
	import static android.opengl.GLES20.glGetProgramiv;
	import static android.opengl.GLES20.glGetProgramInfoLog;
	import static android.opengl.GLES20.GL_VALIDATE_STATUS;

	import static android.opengl.GLES20.glViewport;
	import static android.opengl.GLES20.GL_FLOAT;

	import static android.opengl.Matrix.orthoM;
	import static android.opengl.Matrix.setIdentityM;
	import static android.opengl.Matrix.translateM;
	import static android.opengl.Matrix.multiplyMM;
	import static android.opengl.Matrix.rotateM
;	import static android.opengl.Matrix.scaleM;
	
	import static android.opengl.GLES20.glUniform4f;
	import static android.opengl.GLES20.glUniformMatrix4fv;
	import static android.opengl.GLES20.glGetUniformLocation;
	import static android.opengl.GLES20.glGetAttribLocation;
	import static android.opengl.GLES20.glVertexAttribPointer;
	import static android.opengl.GLES20.glEnableVertexAttribArray;

	import static android.opengl.GLES20.glEnable;
	import static android.opengl.GLES20.glDrawArrays;
	import static android.opengl.GLES20.GL_TRIANGLES;
	import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
	import static android.opengl.GLES20.GL_TRIANGLE_FAN;
    import static android.opengl.GLES20.GL_POINTS;
    
	
	import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
	import static android.opengl.GLES20.GL_DEPTH_TEST;
	import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
	import static android.opengl.GLES20.GL_CULL_FACE;
	import static android.opengl.GLES20.GL_BACK;
	import static android.opengl.GLES20.GL_LEQUAL;

	import static android.opengl.GLES20.glClear;
	import static android.opengl.GLES20.glClearColor;
	import static android.opengl.GLES20.glClearDepthf;
	import static android.opengl.GLES20.glDepthFunc;
	import static android.opengl.GLES20.glDepthMask;
	import static android.opengl.GLES20.glCullFace;

	import static android.opengl.GLES20.glGenTextures;
	import static android.opengl.GLES20.glBindTexture;
	import static android.opengl.GLES20.GL_TEXTURE_2D;
	import static android.opengl.GLES20.glTexParameteri;
	import static android.opengl.GLES20.glActiveTexture;
	import static android.opengl.GLES20.GL_TEXTURE0;
	import static android.opengl.GLES20.glUniform1i;

	import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
	import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
	import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
	import static android.opengl.GLES20.GL_LINEAR;
	import static android.opengl.GLES20.glGenerateMipmap;


public class MainActivity extends Activity implements SensorEventListener
{
	private GLSurfaceView surf;
	public RenderClass rc;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		SensorManager sensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor accel = sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensor.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
		

		surf = new GLSurfaceView(this);
		surf.setEGLContextClientVersion(2);
		rc = new RenderClass(this);
		surf.setRenderer(rc);
        setContentView(surf);

	}


	@Override
	protected void onPause(){
		super.onPause();
		surf.onPause();
	}
	@Override
	protected void onResume(){

		super.onResume();
		surf.onResume();
	}


	public void onSensorChanged(SensorEvent event) {

		rc.accelX = event.values[0];
		rc.accelY = event.values[1];
		rc.accelZ= event.values[2];


	}
	public void onAccuracyChanged(Sensor sensor, int changed){}

	


}//Activity



class RenderClass implements Renderer {
    public float accelX;
	public float accelY;
	public float accelZ;
	
	private final Context context;
	private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix=new float[16];
	private final float[] temp= new float[16];		

	private Table table;
	private int rot=0;
	private ArrayList<Table> tables= new ArrayList<Table>();
	
	private ColorShaderProgram textureProgram;

	//private int texture;

	public RenderClass(Context context){
		this.context=context;
       
	}

	///// Runnables

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config){
		glClearColor(0f,.04f,.1f,1f);
        for (int i=0; i<=8; i++){
		table= new Table();
		table.inc=1+i*4;
		tables.add(table);
		}
		textureProgram=new ColorShaderProgram(context);
//		colorProgram=new ColorShaderProgram(context);

		//texture= ShaderHelper.loadTexture(context, 0);
	    // or R.drawable.table)

		//glEnable(GL_DEPTH_TEST);
        //glClearDepthf(1.0f);
		//glDepthFunc( GL_LEQUAL );
        //glDepthMask( true );
		//glCullFace(GL_BACK);

	}//onSurfaceCreated

	@Override 
	public void onSurfaceChanged(GL10 unused, int w, int h){


		glViewport(0,0,w,h);
        ShaderHelper.perspectiveM(projectionMatrix, 45, (float)w/(float)h,2f,10f);

		setIdentityM(modelMatrix,0);
		translateM(modelMatrix,0,0f,0f, -8f);
		multiplyMM(temp,0,projectionMatrix,0, modelMatrix,0);
		System.arraycopy(temp,0,projectionMatrix,0,temp.length);


	}//onSurfaceChanged


	///////// DRAW ////////
	@Override 
	public void onDrawFrame(GL10 unused){

        rot++;
		setIdentityM(modelMatrix,0);
		rotateM(modelMatrix, 0, 1,0,0,1);
		multiplyMM(temp,0,projectionMatrix,0, modelMatrix,0);
		System.arraycopy(temp,0,projectionMatrix,0,temp.length);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		textureProgram.useProgram();
		//textureProgram.setUniforms(projectionMatrix, texture);
		
		for(int i=0; i<tables.size(); i++){
			setIdentityM(modelMatrix,0);
		    rotateM(modelMatrix,0,tables.get(i).inc*rot/40,accelX,accelY, accelZ);
			
			multiplyMM(temp,0,projectionMatrix,0, modelMatrix,0);
			//System.arraycopy(temp,0,projectionMatrix,0,temp.length);
			textureProgram.setUniforms(temp);
	    
		tables.get(i).bindData(textureProgram);
		tables.get(i).draw();
		
}


	}////// DRAW //////
	////////////////////////////
	////////////////////////////
	////// Helper Classes //////



	

	class Table {
		private static final int stride=( 3)*4;
		
        private final float [] vertices = new float[400];
        private final float[] textureVerts =new float[280];
		
        private float[] matrix=new float[16];
		

		private final VertexArray vertexArray;
		private final VertexArray colorArray;
        private float inc;

		public Table(){
			
			
			for (int n=0; n<=360; n+=6) {
				inc+=.6;
				vertices[n]=(float) Math.cos(inc)-(float)Math.sin(inc);
				vertices[n+1]=(float)Math.sin(inc)+(float)Math.cos(inc);
				vertices[n+2]=inc/2;
				vertices[n+3]=(float) Math.cos(inc)-(float)Math.sin(inc);
				vertices[n+4]=(float) Math.sin(inc)*(float)Math.cos(inc);
				vertices[n+5]=inc/3;
			}
			for (int n=0; n<=240; n+=6){
				
				textureVerts[n]=1f;
				textureVerts[n+1]=0f;
				textureVerts[n+2]=.4f;
				textureVerts[n+4]=1;
				textureVerts[n+5]=.6f;
				textureVerts[n+6]=1;
			
				
			}
			
			vertexArray = new VertexArray(vertices);
			colorArray=new VertexArray(textureVerts);
		}//Table constructor

		public void bindData(ColorShaderProgram textureProgram){
			vertexArray.setVertexAttribPointer(0, textureProgram.getPositionAttributeLocation(),3, stride);
			colorArray.setVertexAttribPointer(0, textureProgram.getColorAttributeLocation(),3, 8);
		}
		public void draw(){
			
			glDrawArrays(GL_TRIANGLE_STRIP,0,30);
		

		}
	}//table (other objects similar)



	class VertexArray{
		private final FloatBuffer floatBuffer;

		public VertexArray(float[] vertexData){
			floatBuffer =ByteBuffer.allocateDirect(vertexData.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
		}//constructor
		public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride){
			floatBuffer.position(dataOffset);
			glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false,stride,floatBuffer);
			glEnableVertexAttribArray(attributeLocation);
			floatBuffer.position(0);
		}//setVertexAttrib

	}//VertexArray


}//renderclass




	



	





 
 
 

