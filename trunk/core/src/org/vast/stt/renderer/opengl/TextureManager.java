/***************************************************************
 (c) Copyright 2005, University of Alabama in Huntsville (UAH)
 ALL RIGHTS RESERVED

 This software is the property of UAH.
 It cannot be duplicated, used, or distributed without the
 express written consent of UAH.

 This software developed by the Vis Analysis Systems Technology
 (VAST) within the Earth System Science Lab under the direction
 of Mike Botts (mike.botts@atmos.uah.edu)
 ***************************************************************/

package org.vast.stt.renderer.opengl;

import java.nio.ByteBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.vast.stt.style.DataStyler;
import org.vast.stt.style.RasterPixelGraphic;
import org.vast.stt.style.RasterTileGraphic;
import org.vast.stt.style.TextureMappingStyler;


/**
 * <p><b>Title:</b><br/>
 * Texture Manager
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Generated POT or NPOT textures according to OpenGL hardware 
 * capabilities. POT textures can be generated by resampling or
 * padding with 100% transparent white pixels, in which case
 * texture coordinates are automatically adjusted.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Apr 13, 2006
 * @version 1.0
 */
public class TextureManager
{
    protected GL gl;
    protected GLU glu;
    protected boolean npotSupported;
    protected boolean normalizationRequired;
    
    
    public TextureManager(GL gl, GLU glu)
    {
        this.gl = gl;
        this.glu = glu;
        
        // find out which texture 2D target to use
        String glExtensions = gl.glGetString(GL.GL_EXTENSIONS);
        if (glu.gluCheckExtension("GL_ARB_texture_rectangle", glExtensions) ||
            glu.gluCheckExtension("GL_EXT_texture_rectangle", glExtensions))
        {
            OpenGLCaps.TEXTURE_2D_TARGET = GL.GL_TEXTURE_RECTANGLE_EXT;
            System.err.println("--> NPOT textures supported <--");
            npotSupported = true;
            normalizationRequired = false;
        }
        else
        {
            OpenGLCaps.TEXTURE_2D_TARGET = GL.GL_TEXTURE_2D;
            System.err.println("--> NPOT textures NOT supported <--");
            System.err.println("--> Textures will be padded with transparent pixels or resampled <--");
            npotSupported = false;
            normalizationRequired = true;
        }
        
        // enable right texture target
        gl.glEnable(OpenGLCaps.TEXTURE_2D_TARGET);
    }
    
    
    /**
     * Retrieves stored textureID or create a new one along
     * with the corresponding texture in OpenGL memory.
     * @param tex
     * @return
     */
    public void useTexture(DataStyler styler, RasterTileGraphic tex)
    {
        GLTextureInfo texInfo = (GLTextureInfo)tex.info.rendererParams;
        if (texInfo != null)
        {
            // if block was updated delete previous texture
            if (tex.info.updated)
            {
                if (texInfo.textureID != -1)
                    gl.glDeleteTextures(1, new int[] {texInfo.textureID}, 0);
            }
            
            // otherwise just bind texture
            else 
            {
                gl.glBindTexture(OpenGLCaps.TEXTURE_2D_TARGET, texInfo.textureID);
                return;
            }
        }
        else
        {
            texInfo = new GLTextureInfo();
            tex.info.rendererParams = texInfo;
        }
        
        // fetch texture data
        fillRGBAData(styler, tex, texInfo);
        
        // if texture was successfully constructed, bind it with GL
        if (tex.hasRasterData)
        {
            int[] id = new int[1];
            gl.glGenTextures(1, id, 0);
            texInfo.textureID = id[0];
            gl.glBindTexture(OpenGLCaps.TEXTURE_2D_TARGET, texInfo.textureID);
            gl.glTexImage2D(OpenGLCaps.TEXTURE_2D_TARGET, 0, GL.GL_RGBA,
                            tex.width + texInfo.widthPadding, tex.height + texInfo.heightPadding,
                            0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, tex.rasterData);
            
            tex.rasterData = null;
            tex.info.updated = false;
        }
        
        gl.glTexParameteri(OpenGLCaps.TEXTURE_2D_TARGET, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(OpenGLCaps.TEXTURE_2D_TARGET, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
    }
    
    
    /**
     * Create an RGBA texture based on data passed by styler
     * @param styler
     * @param tex
     * @param texInfo
     */
    protected void fillRGBAData(DataStyler styler, RasterTileGraphic tex, GLTextureInfo texInfo)
    {
        int paddedWidth = tex.width;
        int paddedHeight = tex.height;
        int trueWidth = tex.width;
        int trueHeight = tex.height;
        boolean padded = false;
        
        
        // handle case of padding
        if (!npotSupported)
        {
            // determine closest power of 2
            double powerWidth = Math.log(paddedWidth)/Math.log(2);
            double powerHeight = Math.log(paddedHeight)/Math.log(2);

            // compute new width only if not already power of 2
            if (Math.floor(powerWidth) != powerWidth)
            {
                paddedWidth = (int) Math.pow(2, (int) powerWidth + 1);
                padded = true;
            }
            
            // compute new height only if not already power of 2
            if (Math.floor(powerHeight) != powerHeight)
            {
                paddedHeight = (int) Math.pow(2, (int) powerHeight + 1);
                padded = true;
            }
            
            // display warning message if padding is needed
            if (padded)
            {
                System.err.println("Texture will be padded to have a power of two size:");
                System.err.println("   original size: " + trueWidth + " x " + trueHeight);
                System.err.println("     padded size: " + paddedWidth + " x " + paddedHeight);
                
                texInfo.widthPadding = paddedWidth - trueWidth;
                texInfo.heightPadding = paddedHeight - trueHeight;
            }
        }
        
        // create byte buffer of the right size
        byte[] buffer = new byte[paddedWidth*paddedHeight*4];
        int index = 0;
        
        if (styler instanceof TextureMappingStyler)
        {
            for (int j=0; j<trueHeight; j++)
            {
                for (int i=0; i<trueWidth; i++)
                {
                    RasterPixelGraphic pixel = ((TextureMappingStyler)styler).getPixel(i, j);
                    buffer[index] = (byte)pixel.r;
                    index++;
                    buffer[index] = (byte)pixel.g;
                    index++;
                    buffer[index] = (byte)pixel.b;
                    index++;
                    buffer[index] = -1;//(byte)pixel.a;
                    index++;
                }
                
                // skip padding bytes
                index += texInfo.widthPadding*4;
            }
        }
        
        tex.rasterData = ByteBuffer.wrap(buffer);
        tex.hasRasterData = true;
        tex.hasColorMapData = false;
    }
    
    
    /**
     * Create an RGB texture based on data passed by styler
     * @param tex
     */
    protected void fillRGBData(DataStyler styler, RasterTileGraphic texture)
    {
        
    }


    public boolean isNpotSupported()
    {
        return npotSupported;
    }


    public boolean isNormalizationRequired()
    {
        return normalizationRequired;
    }
}
