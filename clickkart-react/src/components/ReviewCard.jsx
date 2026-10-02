import { FaStar, FaTrash } from 'react-icons/fa';
import { formatDate } from '../services/api';

/**
 * ReviewCard — Displays a single review with stars, comment, author, date.
 * @param {object}   review      - { id, userName, rating, comment, createdAt, userId }
 * @param {number}   currentUserId - logged-in user's ID (to show delete button)
 * @param {function} onDelete    - callback(reviewId)
 */
const ReviewCard = ({ review, currentUserId, onDelete }) => {
  const isOwner = currentUserId && review.userId === currentUserId;

  const stars = Array.from({ length: 5 }, (_, i) => (
    <FaStar key={i} style={{ color: i < review.rating ? '#f59e0b' : '#ddd' }} />
  ));

  return (
    <div className="review-card">
      <div className="review-card-header">
        <span className="reviewer">{review.userName || 'Anonymous'}</span>
        <span className="review-date">{formatDate(review.createdAt)}</span>
      </div>
      <div className="review-stars" aria-label={`${review.rating} out of 5 stars`}>
        {stars}
      </div>
      {review.comment && (
        <p className="review-comment">{review.comment}</p>
      )}
      {isOwner && onDelete && (
        <button
          className="delete-review-btn"
          onClick={() => onDelete(review.id)}
          aria-label="Delete your review"
        >
          <FaTrash /> Delete
        </button>
      )}
    </div>
  );
};

export default ReviewCard;
