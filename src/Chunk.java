import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {
  static final int CHUNK_SIZE = 30;
  static final int CUBE_LENGTH = 2;
  private Block[][][] Blocks;
  private int VBOVertexHandle;
  private int VBOColorHandle;
  private int StartX, StartY, StartZ;
  private Random r;

  private int VBOTextureHandle;
  private Texture texture;

  public Chunk (int startX, int startY, int startZ) {
    try
    {
      texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("terrain.png"));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    r = new Random(System.currentTimeMillis());
    Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    for (int x = 0; x < CHUNK_SIZE; x++) {
      for (int y = 0; y < CHUNK_SIZE; y++) {
        for (int z = 0; z < CHUNK_SIZE; z++) {
          float f = r.nextFloat();
          if (f > 0.85f)
          Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Grass);
          else if (f > 0.7f)
          Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Dirt);
          else if (f > 0.55f)
          Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Water);
          else if (f > 0.4f)
          Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Sand);
          else if (f > 0.2f)
          Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
          else
          Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Bedrock);
        }
      }
    }

    VBOColorHandle = glGenBuffers();
    VBOVertexHandle = glGenBuffers();
    VBOTextureHandle = glGenBuffers();
    StartX = startX;
    StartY = startY;
    StartZ = startZ;
    rebuildMesh(startX, startY, startZ);
  }

  public void render() {
    glPushMatrix();
    glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
    glVertexPointer(3, GL_FLOAT, 0, 0L);
    glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
    glColorPointer(3, GL_FLOAT, 0, 0L);
    glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
    glBindTexture(GL_TEXTURE_2D, 1);
    glTexCoordPointer(2, GL_FLOAT, 0, 0L);
    glDrawArrays(GL_QUADS, 0, CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE*24);
    glPopMatrix();
  }

  public void rebuildMesh (float startX, float startY, float startZ) {
    VBOColorHandle = glGenBuffers();
    VBOVertexHandle = glGenBuffers();
    VBOTextureHandle = glGenBuffers();

    FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer ((
    CHUNK_SIZE *CHUNK_SIZE * CHUNK_SIZE) *6 * 12);
    FloatBuffer VertexColorData = BufferUtils.createFloatBuffer((CHUNK_SIZE *
    CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
    FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer((CHUNK_SIZE *
    CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);


    SimplexNoise noise = new SimplexNoise(2, 0.1, (int)System.currentTimeMillis());


    for (float x = 0; x < CHUNK_SIZE; x++)
    {
      r = new Random(System.currentTimeMillis());
      for (float z = 0; z < CHUNK_SIZE; z++)
      {
        double height = 0.0;
        int octaves = 18;
        for (int resolution = octaves; resolution > 1; resolution--)
        {
          for (int y = 0; y < CHUNK_SIZE; ++y)
          {
            int i = (int)(startX + x / resolution);
            int j = (int)(startY + y / resolution);
            int k = (int)(startZ + z / resolution);

            double temp = startY + (1 + noise.getNoise(i, j, k)) * CUBE_LENGTH / 2;
            height += (int) temp;
          }
        }

        height /= octaves;
        height = (int) height;
        int max = 0;

        for (float y = 0; y < CHUNK_SIZE; y++) {
          if (y < height)
          {
            Block.BlockType newType = Block.BlockType.BlockType_Water;
            if (y == height - 1)
            {
              //double rand = noise.getNoise((int)x,(int)z) * 10;
              //System.out.println(height);
              if (y >= 15 && y < 18) {
                newType = Block.BlockType.BlockType_Water;
              }
              else if (y >= 18 && y <= 20) {
                newType = Block.BlockType.BlockType_Sand;
              }
              else if (y > 20 && y < 30) {
                newType = Block.BlockType.BlockType_Grass;
              }
            }
            else if (y > 5)
            {
              int rand = r.nextInt(2);

              if (rand == 0)
              newType = Block.BlockType.BlockType_Dirt;
              else
              newType = Block.BlockType.BlockType_Stone;
            }
            else if (y > 0)
            {
              int rand = r.nextInt(3);

              switch (rand)
              {
                case 0:
                {
                  newType = Block.BlockType.BlockType_Dirt;
                  break;
                }
                case 1:
                {
                  newType = Block.BlockType.BlockType_Stone;
                  break;
                }
                case 2:
                {
                  newType = Block.BlockType.BlockType_Bedrock;
                  break;
                }
              }
            }
            else
            {
              newType = Block.BlockType.BlockType_Bedrock;
            }
            Blocks[(int)x][(int)y][(int)z].setType(newType);

            VertexPositionData.put(createCube((float)(startX + x
            * CUBE_LENGTH), (float)(y * CUBE_LENGTH + (int)
            (CHUNK_SIZE * .8)), (float)(startZ + z *
            CUBE_LENGTH)));
            VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[(int)x][(int) y][(int) z])));
            VertexTextureData.put(createTexCube((float) 0, (float) 0,
            Blocks[(int)(x)][(int)(y)][(int)(z)]));
          }
        }
      }
    }

    VertexColorData.flip();
    VertexPositionData.flip();
    VertexTextureData.flip();
    glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
    glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
    glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
    glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  private float[] createCubeVertexCol(float[] CubeColorArray) {
    float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
    for (int i = 0; i < cubeColors.length; i++) {
      cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
    }
    return cubeColors;
  }

  public static float[] createTexCube(float x, float y, Block blk) {
    float offset = (1024f / 16) / 1024f;

    switch(blk.getID()) {
      case 0:
      return new float[] {
        //grass with green wool top

        // TOP!
        x + offset*3, y + offset*10,
        x + offset*2, y + offset*10,
        x + offset*2, y + offset*9,
        x + offset*3, y + offset*9,

        // BOTTOM QUAD(DOWN=+Y)
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,
        // FRONT QUAD
        x + offset*3, y + offset*0,
        x + offset*4, y + offset*0,
        x + offset*4, y + offset*1,
        x + offset*3, y + offset*1,
        // BACK QUAD
        x + offset*4, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*0,
        x + offset*4, y + offset*0,
        // LEFT QUAD
        x + offset*3, y + offset*0,
        x + offset*4, y + offset*0,
        x + offset*4, y + offset*1,
        x + offset*3, y + offset*1,
        // RIGHT QUAD
        x + offset*3, y + offset*0,
        x + offset*4, y + offset*0,
        x + offset*4, y + offset*1,
        x + offset*3, y + offset*1
      };

      case 1:
      return new float[] {
        //sand

        // TOP!
        x + offset*2, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*2,
        x + offset*2, y + offset*2,

        // BOTTOM QUAD(DOWN=+Y)
        x + offset*2, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*2,
        x + offset*2, y + offset*2,
        // FRONT QUAD
        x + offset*2, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*2,
        x + offset*2, y + offset*2,
        // BACK QUAD
        x + offset*2, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*2,
        x + offset*2, y + offset*2,
        // LEFT QUAD
        x + offset*2, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*2,
        x + offset*2, y + offset*2,
        // RIGHT QUAD
        x + offset*2, y + offset*1,
        x + offset*3, y + offset*1,
        x + offset*3, y + offset*2,
        x + offset*2, y + offset*2
      };
      case 2:
      return new float[] {
        //water

        // TOP!
        x + offset*14, y + offset*0,
        x + offset*15, y + offset*0,
        x + offset*15, y + offset*1,
        x + offset*14, y + offset*1,

        // BOTTOM QUAD(DOWN=+Y
        x + offset*14, y + offset*0,
        x + offset*15, y + offset*0,
        x + offset*15, y + offset*1,
        x + offset*14, y + offset*1,
        // FRONT QUAD
        x + offset*14, y + offset*0,
        x + offset*15, y + offset*0,
        x + offset*15, y + offset*1,
        x + offset*14, y + offset*1,
        // BACK QUAD
        x + offset*14, y + offset*0,
        x + offset*15, y + offset*0,
        x + offset*15, y + offset*1,
        x + offset*14, y + offset*1,
        // LEFT QUAD
        x + offset*14, y + offset*0,
        x + offset*15, y + offset*0,
        x + offset*15, y + offset*1,
        x + offset*14, y + offset*1,
        // RIGHT QUAD
        x + offset*14, y + offset*0,
        x + offset*15, y + offset*0,
        x + offset*15, y + offset*1,
        x + offset*14, y + offset*1,
      };
      case 3:
      return new float[] {
        //dirt

        // TOP!
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,

        // BOTTOM QUAD(DOWN=+Y)
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,
        // FRONT QUAD
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,
        // BACK QUAD
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,
        // LEFT QUAD
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,
        // RIGHT QUAD
        x + offset*3, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*0,
        x + offset*3, y + offset*0,
      };
      case 4:
      return new float[] {
        //stone

        // TOP!
        x + offset*1, y + offset*0,
        x + offset*2, y + offset*0,
        x + offset*2, y + offset*1,
        x + offset*1, y + offset*1,

        // BOTTOM QUAD(DOWN=+Y)
        x + offset*1, y + offset*0,
        x + offset*2, y + offset*0,
        x + offset*2, y + offset*1,
        x + offset*1, y + offset*1,
        // FRONT QUAD
        x + offset*1, y + offset*0,
        x + offset*2, y + offset*0,
        x + offset*2, y + offset*1,
        x + offset*1, y + offset*1,
        // BACK QUAD
        x + offset*1, y + offset*0,
        x + offset*2, y + offset*0,
        x + offset*2, y + offset*1,
        x + offset*1, y + offset*1,
        // LEFT QUAD
        x + offset*1, y + offset*0,
        x + offset*2, y + offset*0,
        x + offset*2, y + offset*1,
        x + offset*1, y + offset*1,
        // RIGHT QUAD
        x + offset*1, y + offset*0,
        x + offset*2, y + offset*0,
        x + offset*2, y + offset*1,
        x + offset*1, y + offset*1,
      };
      case 5:
      return new float[] {
        //bedrock

        // TOP!
        x + offset*1, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*2,
        x + offset*1, y + offset*2,

        // BOTTOM QUAD(DOWN=+Y)
        x + offset*1, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*2,
        x + offset*1, y + offset*2,
        // FRONT QUAD
        x + offset*1, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*2,
        x + offset*1, y + offset*2,
        // BACK QUAD
        x + offset*1, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*2,
        x + offset*1, y + offset*2,
        // LEFT QUAD
        x + offset*1, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*2,
        x + offset*1, y + offset*2,
        // RIGHT QUAD
        x + offset*1, y + offset*1,
        x + offset*2, y + offset*1,
        x + offset*2, y + offset*2,
        x + offset*1, y + offset*2,
      };
    }
    return null;

  }

  public static float[] createCube(float x, float y, float z) {
    int offset = CUBE_LENGTH / 2;
    return new float[] {
      //Top Quad
      x + offset, y + offset, z,
      x - offset, y + offset, z,
      x - offset, y + offset, z - CUBE_LENGTH,
      x + offset, y + offset, z - CUBE_LENGTH,

      //Bottom Quad
      x + offset, y - offset, z - CUBE_LENGTH,
      x - offset, y - offset, z - CUBE_LENGTH,
      x - offset, y - offset, z,
      x + offset, y - offset, z,

      //Front Quad
      x + offset, y + offset, z - CUBE_LENGTH,
      x - offset, y + offset, z - CUBE_LENGTH,
      x - offset, y - offset, z - CUBE_LENGTH,
      x + offset, y - offset, z - CUBE_LENGTH,

      //Back Quad
      x + offset, y - offset, z,
      x - offset, y - offset, z,
      x - offset, y + offset, z,
      x + offset, y + offset, z,

      //Left Quad
      x - offset, y + offset, z - CUBE_LENGTH,
      x - offset, y + offset, z,
      x - offset, y - offset, z,
      x - offset, y - offset, z - CUBE_LENGTH,

      //Right Quad
      x + offset, y + offset, z,
      x + offset, y + offset, z - CUBE_LENGTH,
      x + offset, y - offset, z - CUBE_LENGTH,
      x + offset, y - offset, z };
    }

    private float[] getCubeColor(Block blk) {
      return new float[] {1, 1, 1};
    }
  }
