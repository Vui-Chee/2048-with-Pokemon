package com.example.pokemon2048;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoader {

    public static BufferedImage[][] readyImages(String imageDirName){
        int index = 0;
        BufferedImage[][] animationImages;
		    animationImages = new BufferedImage[151+1][10];
        File[] imageFolders = (new File(imageDirName)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isHidden() && !file.getName().startsWith(".");
            }
        });

        int id = 0;
        for (File folder : imageFolders){
            /* Only 152 images. Last index is 151. */
            int folderIndex = Integer.parseInt(folder.getName()) - 1;
            try {
                String imageDirPath = imageDirName + "/" + folder.getName();
                File[] animationImageFiles = (new File(imageDirPath)).listFiles(new FileFilter(){
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".png");
                    }              
                });
                for (File f : animationImageFiles){
                    if (f.getName().length() < 0){
                        System.out.println("Name length of image too short.");
                    }
                    int imageIndex = f.getName().charAt(0) - 48;
                    animationImages[folderIndex][imageIndex] = ImageIO.read(f);
                }
            } catch(IOException e){
                System.out.println("Image io error.");
            }
            id++;
        }

        return animationImages;
    }
} // End of ImageLoader
