import sharp from 'sharp';

import { errorHandler, errorBuilder } from 'errors/errors';
import { getImageById } from 'db/oracledb/images-dao';
import { parseQuery } from 'utils/parse-query';

/**
 * Get image by ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { osuId } = req.params;
    const { width } = parseQuery(req.query);
    const image = await getImageById(osuId);

    if (!image) {
      return errorBuilder(res, 404, 'No image with the specified OSU ID was not found.');
    }

    res.contentType('image/jpeg');
    let result;
    const sharpImage = sharp(image);
    if (width) {
      result = await sharpImage.resize(width, null).toBuffer();
    } else if (await sharpImage.metadata().width > 2000) {
      result = await sharpImage.resize(2000, null).toBuffer();
    } else {
      result = image;
    }

    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
