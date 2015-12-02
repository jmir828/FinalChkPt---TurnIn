/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joswizzle
 */


import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;
import java.nio.FloatBuffer;

public class FPCameraController
{
  private Vector3f position = null;
  private Vector3f lPosition = null;
  private float yaw = 0.0f;
  private float pitch = 0.0f;
  private Vector3Float me;

  private Chunk chunk = new Chunk(0,0,0);
  private Chunk chunk1 = new Chunk(60,0,0);
  private Chunk chunk2 = new Chunk(0,0,60);
  private Chunk chunk3 = new Chunk(60,0,60);
  private Chunk chunk4 = new Chunk(-60,0,60);
  private Chunk chunk5 = new Chunk(-60,0,-60);
  private Chunk chunk6 = new Chunk(60,0,-60);
  private Chunk chunk7 = new Chunk(0,0,-60);
  private Chunk chunk8 = new Chunk(-60,0,0);

  public FPCameraController(float x,float y, float z)
  {
    position = new Vector3f(x, y, z);
    lPosition = new Vector3f(x, y, z);

    lPosition.x = 30f;
    lPosition.y = 70f;
    lPosition.z = 30f;
  }

  public void yaw(float amount)
  {
    yaw += amount;
  }

  public void pitch(float amount)
  {
    pitch -= amount;
  }

  public void walkForward(float distance)
  {
    float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
    float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
    position.x -= xOffset;
    position.z += zOffset;

    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
    glLight(GL_LIGHT0, GL_POSITION, lightPosition);
  }

  public void walkBackwards(float distance)
  {
    float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
    float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
    position.x += xOffset;
    position.z -= zOffset;

    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
    glLight(GL_LIGHT0, GL_POSITION, lightPosition);
  }

  public void strafeLeft(float distance)
  {
    float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
    float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
    position.x -= xOffset;
    position.z += zOffset;

    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
    glLight(GL_LIGHT0, GL_POSITION, lightPosition);
  }

  public void strafeRight(float distance)
  {
    float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
    float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
    position.x -= xOffset;
    position.z += zOffset;

    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
    glLight(GL_LIGHT0, GL_POSITION, lightPosition);
  }

  public void moveUp(float distance)
  {
    position.y -= distance;
  }

  public void moveDown(float distance)
  {
    position.y += distance;
  }

  public void lookThrough()
  {
    glRotatef(pitch, 1.0f, 0.0f, 0.0f);
    glRotatef(yaw, 0.0f, 1.0f, 0.0f);
    glTranslatef(position.x, position.y, position.z);

    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
    lightPosition.put(lPosition.x).put(lPosition.y).put(lPosition.z).put(
    1.0f).flip();
    glLight(GL_LIGHT0, GL_POSITION, lightPosition);

  }

  public void gameLoop()
  {
    FPCameraController camera = new FPCameraController(-30,-80,-160);
    float dx = 0.0f;
    float dy = 0.0f;
    float dt = 0.0f;

    float lastTime = 0.0f;
    long time = 0;
    float mouseSensitivity = 0.09f;
    float movementSpeed = 0.6f;

    Mouse.setGrabbed(true);

    while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
    {
      time = Sys.getTime();
      lastTime = time;

      dx = Mouse.getDX();
      dy = Mouse.getDY();

      camera.yaw(dx * mouseSensitivity);
      camera.pitch(dy * mouseSensitivity);

      if (Keyboard.isKeyDown(Keyboard.KEY_W))
      {
        camera.walkForward(movementSpeed);
      }
      if (Keyboard.isKeyDown(Keyboard.KEY_S))
      {
        camera.walkBackwards(movementSpeed);
      }
      if (Keyboard.isKeyDown(Keyboard.KEY_A))
      {
        camera.strafeLeft(movementSpeed);
      }
      if (Keyboard.isKeyDown(Keyboard.KEY_D))
      {
        camera.strafeRight(movementSpeed);
      }
      if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
      {
        camera.moveUp(movementSpeed);
      }
      if (Keyboard.isKeyDown(Keyboard.KEY_E))
      {
        camera.moveDown(movementSpeed);
      }

      glLoadIdentity();

      camera.lookThrough();
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      chunk.render();
      chunk1.render();
      chunk2.render();
      chunk3.render();
      chunk4.render();
      chunk5.render();
      chunk6.render();
      chunk7.render();
      chunk8.render();

      Display.update();
      Display.sync(60);
    }
    Display.destroy();
  }

  private void render()
  {
    try
    {
      glBegin(GL_QUADS);
      //front
      glColor3f(0.0f, 0.0f, 1.0f);
      glVertex3f( 1.0f, 1.0f, 1.0f);
      glVertex3f(-1.0f, 1.0f, 1.0f);
      glVertex3f(-1.0f,-1.0f, 1.0f);
      glVertex3f( 1.0f,-1.0f, 1.0f);
      //back
      glColor3f(0.0f, 1.0f, 0.0f);
      glVertex3f(-1.0f, 1.0f,-1.0f);
      glVertex3f( 1.0f, 1.0f,-1.0f);
      glVertex3f( 1.0f,-1.0f,-1.0f);
      glVertex3f(-1.0f,-1.0f,-1.0f);
      //top
      glColor3f(0.0f, 1.0f, 1.0f);
      glVertex3f( 1.0f, 1.0f,-1.0f);
      glVertex3f(-1.0f, 1.0f,-1.0f);
      glVertex3f(-1.0f, 1.0f, 1.0f);
      glVertex3f( 1.0f, 1.0f, 1.0f);
      //bottom
      glColor3f(1.0f, 0.0f, 0.0f);
      glVertex3f( 1.0f,-1.0f, 1.0f);
      glVertex3f(-1.0f,-1.0f, 1.0f);
      glVertex3f(-1.0f,-1.0f,-1.0f);
      glVertex3f( 1.0f,-1.0f,-1.0f);
      //left
      glColor3f(1.0f, 0.0f, 1.0f);
      glVertex3f(-1.0f, 1.0f, 1.0f);
      glVertex3f(-1.0f, 1.0f,-1.0f);
      glVertex3f(-1.0f,-1.0f,-1.0f);
      glVertex3f(-1.0f,-1.0f, 1.0f);
      //right
      glColor3f(1.0f, 1.0f, 0.0f);
      glVertex3f( 1.0f, 1.0f,-1.0f);
      glVertex3f( 1.0f, 1.0f, 1.0f);
      glVertex3f( 1.0f,-1.0f, 1.0f);
      glVertex3f( 1.0f,-1.0f,-1.0f);
      glEnd();
    }
    catch(Exception e)
    {

    }
  }


}
