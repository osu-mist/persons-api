import fs from 'fs';
import sharp from 'sharp';

import { errorHandler } from 'errors/errors';
import { getImageById } from 'db/oracledb/images-dao';
import { openapi } from 'utils/load-openapi';
import { personExists } from 'db/oracledb/persons-dao';

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
    const internalId = await personExists(osuId);
    let image;

    if (internalId) {
      image = await getImageById(internalId);
    }

    // load default image if image is null
    if (!image) {
      image = fs.readFileSync(`${__dirname}/../../../resources/defaultImage.jpg`);
    }

    let result;
    const sharpImage = sharp(image);
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
