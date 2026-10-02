import { FaStar, FaStarHalfAlt, FaRegStar } from 'react-icons/fa';

/**
 * AverageRating — Display read-only star rating with average and count.
 * @param {number} average - average rating (0–5)
 * @param {number} count - total number of reviews
 */
const AverageRating = ({ average = 0, count = 0 }) => {
  const stars = [];
  const rounded = Math.round(average * 2) / 2; // round to nearest 0.5

  for (let i = 1; i <= 5; i++) {
    if (rounded >= i) {
      stars.push(<FaStar key={i} />);
    } else if (rounded >= i - 0.5) {
      stars.push(<FaStarHalfAlt key={i} />);
    } else {
      stars.push(<FaRegStar key={i} />);
    }
  }

  return (
    <div className="review-summary" role="group" aria-label={`Average rating: ${average} out of 5, based on ${count} reviews`}>
      <span className="avg-rating">{average > 0 ? average.toFixed(1) : '0.0'}</span>
      <span className="star-display" aria-hidden="true">{stars}</span>
      <span>({count} review{count !== 1 ? 's' : ''})</span>
    </div>
  );
};

export default AverageRating;
