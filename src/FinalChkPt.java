/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joswizzle
 */

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class FinalChkPt {
    private FPCameraController fp;
    private DisplayMode displayMode = new DisplayMode(640,480);
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;

    public void start() {
        try {
            createWindow();
            initGL();
            fp = new FPCameraController(0f, 0f, 0f);
            fp.gameLoop();
        } catch(Exception exc) { exc.printStackTrace(); }
    }

    public void createWindow() throws Exception {
        Display.setFullscreen(false);
        DisplayMode[] d = Display.getAvailableDisplayModes();
        
        for (int i = 0; i < d.length; ++i) {
            if (d[i].getWidth() == 640 && d[i].getHeight() == 480 && d[i].getBitsPerPixel() == 32) {
                displayMode = d[i];
                break;
            }
        }

        Display.setDisplayMode(displayMode);
        Display.setTitle("Final Check Point");
        Display.create();
    }

    public void initGL() {
        glClearColor(0f, 0f, 0f, 0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float)displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
    }


    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();

        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(2.0f).put(2.0f).put(2.0f).put(0.0f).flip();
    }

    public static void main(String[] args) {
        FinalChkPt checkPoint = new FinalChkPt();
        checkPoint.start();
    }
}