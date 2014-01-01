package grailsnail.gl.spiralframe;

import android.opengl.GLES20.*;
import android.opengl.GLUtils.*;
import android.opengl.Matrix.*;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.Random;
import android.util.Log;

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
import static android.opengl.Matrix.rotateM;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;


class ShaderHelper{

	static Bitmap bitmap;
	public static String readResource(Context context, int resId){

		StringBuilder body= new StringBuilder();
		///////////////
		try{
			InputStream is = context.getResources().openRawResource(resId);
			InputStreamReader isr=new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String nextLine;
			while((nextLine=br.readLine()) != null){
				body.append(nextLine);
				body.append("\n");
			} //while

		} catch (java.io.IOException e) {
			throw new RuntimeException("read fail");
			//			catch(Resources.NotFoundException nfe){
			//					throw new RuntimeException("Resource not found");
			//		        }//catch nfe
			//		}//ioe
		}//try

		/////////////////////
		//Log.d("body: ", body.toString());
		return body.toString();
	}//read

	public static int buildProgram(String vertexShaderSource,String fragmentShaderSource){
		int program;
		int vertexShader=compileVertexShader(vertexShaderSource);
		int fragmentShader=compileFragmentShader(fragmentShaderSource);
		program=linkProgram(vertexShader, fragmentShader);
		return program;
	}

	public static int compileVertexShader(String shaderCode){
		return compileShader(GL_VERTEX_SHADER, shaderCode);
	}//compileVertex

	public static int compileFragmentShader(String shaderCode){
		return compileShader(GL_FRAGMENT_SHADER, shaderCode);
	}//compileFramgment

	public static int compileShader(int type, String shaderCode){
		final int shaderObjectId=glCreateShader(type);
		glShaderSource(shaderObjectId, shaderCode);
		glCompileShader(shaderObjectId);

		return shaderObjectId;
	}//compileShader

	public static int linkProgram(int vId, int fId){

		final int programObjectId =glCreateProgram();
		glAttachShader(programObjectId, vId);
		glAttachShader(programObjectId, fId);
		glLinkProgram(programObjectId);

		return programObjectId;

	}//linkProgram

	public static boolean validateProgram(int Id){
		glValidateProgram(Id);
		final int[] validateStatus=new int[1];
		glGetProgramiv(Id, GL_VALIDATE_STATUS, validateStatus, 0);
		return validateStatus[0] != 0;

	}//validate
	public static int loadTexture(Context context, int resId){
		//public static int loadTexture(Context context, int resId){
		final int [] textureObjIds = new int[1];
		glGenTextures(1, textureObjIds, 0);

		final BitmapFactory.Options options= new BitmapFactory.Options();
		options.inScaled=false;

		try{
			InputStream is= context.getAssets().open("texture.jpg");
			bitmap= BitmapFactory.decodeStream(is);
		} catch(IOException ioe){		}

		//final Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(), resId, options);



		glBindTexture(GL_TEXTURE_2D, textureObjIds[0]);

		glTexParameteri(GL_TEXTURE_2D ,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D ,GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		texImage2D(GL_TEXTURE_2D, 0,bitmap,0);
		bitmap.recycle();
		glGenerateMipmap(GL_TEXTURE_2D);
		//glBindTexture(GL_TEXTURE_2D,0);

		return textureObjIds[0];

		/////
		//glActiveTexture(GL_TEXTURE0);
		//uTextureLoc= glGetUniformLocation(program, "u_TextureUnit");
		//aTextureCoordLoc=glGetAttribLocation(program, "a_TextureCoordinates");
		//vertexBuffer.position(6);
		//glVertexAttribPointer(aTextureCoordLoc, 2, GL_FLOAT, false, STRIDE, vertexBuffer);
		//glEnableVertexAttribArray(aTextureCoordLoc);
		////////

	}///loadTex
	public static void perspectiveM(float[] m, float yFovDeg, float aspect, float n, float f){
		final float angleRadians= (float) (yFovDeg*Math.PI/180.0);
		final float a= (float)(1.0/Math.tan(angleRadians/2.0));
		m[0] = a/aspect;
		m[1]=0;
		m[2]=0;
		m[3]=0; 
		m[4]=0;
		m[5]=a;
		m[6]=0;
		m[7]=0;
		m[8]=0; 
		m[9]=0;
		m[10]=-((f+n)/(f-n));
		m[11]=-1f;
		m[12]=0f;
		m[13]=0;
		m[14]=-((2f*f*n)/(f-n));
		m[15]=0;

	}//perspective matrix

}//ShaderHelper
class TextureShaderProgram {
	private int uMatrixLoc;
	private int uTextureUnitLoc;
	private final int aPositionLoc;
	private final int aTextureCoordinatesLoc;
	protected final int program;
//	protected ShaderProgram(Context context, int vertexShaderResId, int fragmentShaderResId) {
//  program = ShaderHelper.buildProgram(ShaderHelper.readResource(context, vertexShaderResId), ShaderHelper.readResource(fragmentShaderResId));





	public TextureShaderProgram(Context context){
		program = ShaderHelper.buildProgram(ShaderHelper.readResource(context, R.raw.vert), ShaderHelper.readResource(context,R.raw.frag));

		//super(context, R.raw.vert, R.raw.frag);
		uMatrixLoc= glGetUniformLocation(program, "u_Matrix");
		uTextureUnitLoc= glGetUniformLocation(program, "u_TextureUnit");
		aPositionLoc = glGetAttribLocation(program, "a_Position");	
		aTextureCoordinatesLoc= glGetAttribLocation(program, "a_TextureCoordinates");

	}
	public void setUniforms(float[] matrix, int textureId){
		glUniformMatrix4fv(uMatrixLoc, 1, false, matrix, 0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureId);
		glUniform1i(uTextureUnitLoc,0);

	}
	public void useProgram(){
		glUseProgram(program);
	}
	public int getPositionAttributeLocation(){
		return aPositionLoc;
	}
	public int getTextureCoordinatesAttributeLocation(){
		return aTextureCoordinatesLoc;
	}
}//textureshader


class ColorShaderProgram {
	private int uMatrixLoc;
	//private int uTextureUnitLoc;
	private final int aPositionLoc;
	private final int aColorLoc;
	//private final int aTextureCoordinatesLoc;
	protected final int program;
//	protected ShaderProgram(Context context, int vertexShaderResId, int fragmentShaderResId) {
//  program = ShaderHelper.buildProgram(ShaderHelper.readResource(context, vertexShaderResId), ShaderHelper.readResource(fragmentShaderResId));


	public ColorShaderProgram(Context context){
		program = ShaderHelper.buildProgram(ShaderHelper.readResource(context, R.raw.vert), ShaderHelper.readResource(context,R.raw.frag));

		//super(context, R.raw.vert, R.raw.frag);
		uMatrixLoc= glGetUniformLocation(program, "u_Matrix");
		//uTextureUnitLoc= glGetUniformLocation(program, "u_TextureUnit");
		aPositionLoc = glGetAttribLocation(program, "a_Position");	
		aColorLoc = glGetAttribLocation(program, "a_Color");	

		//aTextureCoordinatesLoc= glGetAttribLocation(program, "a_TextureCoordinates");

	}
	public void setUniforms(float[] matrix){
		glUniformMatrix4fv(uMatrixLoc, 1, false, matrix, 0);

		//glActiveTexture(GL_TEXTURE0);
		//glBindTexture(GL_TEXTURE_2D, textureId);
		//glUniform1i(uTextureUnitLoc,0);

	}
	public void useProgram(){
		glUseProgram(program);
	}
	public int getPositionAttributeLocation(){
		return aPositionLoc;
	}
	public int getColorAttributeLocation(){
		return aColorLoc;
	}

}


	
		

	

 
