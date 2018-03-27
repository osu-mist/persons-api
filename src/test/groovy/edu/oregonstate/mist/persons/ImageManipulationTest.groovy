package edu.oregonstate.mist.persons

import edu.oregonstate.mist.personsapi.ImageManipulation
import org.junit.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class ImageManipulationTest {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
    InputStream inputStream = classLoader.getResourceAsStream('sampleimage.jpg')
    BufferedImage sampleImage = ImageIO.read(inputStream)

    private static BufferedImage byteToBufferedImage (byte[] imageDate) {
        ImageIO.read(new ByteArrayInputStream(imageDate))
    }

    @Test
    public void testNoResize() {
        byte[] imageData = ImageManipulation.getImageStream(sampleImage, null)

        assert sampleImage.getWidth() == byteToBufferedImage(imageData).getWidth()
        assert sampleImage.getHeight() == byteToBufferedImage(imageData).getHeight()
    }

    @Test
    public void testResize() {
        Integer width = 175
        byte[] imageData = ImageManipulation.getImageStream(sampleImage, width)
        Integer height = Math.round(sampleImage.getHeight() * (width / sampleImage.getWidth()))

        assert width == byteToBufferedImage(imageData).getWidth()
        assert height == byteToBufferedImage(imageData).getHeight()
    }
}
