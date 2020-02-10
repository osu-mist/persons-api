import _ from 'lodash';

/**
 * Paginate data rows
 *
 * @param {object[]} rows Data rows
 * @param {object} pageQuery Pagination query parameter
 * @returns {*} Paginated data rows
 */
const paginate = (rows, pageQuery) => {
  const pageNumber = parseInt(pageQuery.number, 10);
  const pageSize = parseInt(pageQuery.size, 10);
  const totalPages = Math.ceil(rows.length / pageSize) || 1;
  const paginatedRows = _.slice(rows, (pageNumber - 1) * pageSize, pageNumber * pageSize);
  const isOutOfBounds = pageNumber < 1 || pageNumber > totalPages;
  const nextPage = isOutOfBounds || pageNumber >= totalPages ? null : pageNumber + 1;
  const prevPage = isOutOfBounds || pageNumber <= 1 ? null : pageNumber - 1;

  return {
    paginatedRows,
    totalPages,
    pageNumber,
    pageSize,
    nextPage,
    prevPage,
  };
};

export { paginate };
