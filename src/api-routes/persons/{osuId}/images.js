import fs from 'fs';
import sharp from 'sharp';

import { errorHandler } from 'errors/errors';
import { getImageById } from 'db/oracledb/images-dao';
import { parseQuery } from 'utils/parse-query';
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
    const { width } = parseQuery(req.query);
    const image = await getImageById(osuId);

    // return default image if no image is returned from data source
    if (!image) {
      return fs.readFile('src/resources/defaultImage.jpg', (err, data) => {
        if (err) {
          return errorHandler(res, err);
        }
        res.contentType('image/jpeg');
        return res.send(data);
      });
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
