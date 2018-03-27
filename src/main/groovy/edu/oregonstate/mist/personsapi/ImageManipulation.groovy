package edu.oregonstate.mist.personsapi

import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage

class ImageManipulation {
    public static getImageStream(BufferedImage image, Integer width) {
        if (width) {
            /*
             * Resize image
             */
            Integer height = Math.round(image.getHeight() * (width / image.getWidth()))
            BufferedImage resizedImage = new BufferedImage(width, height, image.getType())
            Graphics2D g2d = resizedImage.createGraphics()
            g2d.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH),0, 0, null)
            g2d.dispose()
            image = resizedImage
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ImageIO.write(image, 'jpeg', outputStream)
        outputStream.toByteArray()
    }
}
