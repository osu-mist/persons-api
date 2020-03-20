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
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }
    res.contentType('image/jpeg');

    if (width) {
      const resizedImage = await sharp(image).resize(width, null).toBuffer();
      return res.send(resizedImage);
    }

    return res.send(image);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
