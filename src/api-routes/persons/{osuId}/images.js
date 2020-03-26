import fs from 'fs';
import sharp from 'sharp';

import { getImageById } from 'db/oracledb/images-dao';
import { errorHandler } from 'errors/errors';
import { openapi } from 'utils/load-openapi';

const { maximum: maxWidth } = openapi.components.parameters.imageWidth.schema;

/**
 * Get image by ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { osuId } = req.params;
    const { width } = req.query;
    const image = await getImageById(osuId);

    let result;
    // load default image if image is null
    const sharpImage = sharp(image || fs.readFileSync('src/resources/defaultImage.jpg'));
    if (width) {
      result = await sharpImage.resize(width, null).toBuffer();
    } else if (await sharpImage.metadata().width > maxWidth) {
      result = await sharpImage.resize(maxWidth, null).toBuffer();
    } else {
      result = image;
    }

    res.contentType('image/jpeg');
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
