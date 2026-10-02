import { useState, useCallback } from 'react';
import { FaStar } from 'react-icons/fa';

/**
 * RatingStars — Interactive star input for submitting ratings.
 * @param {number} rating - current rating (1–5)
 * @param {function} onRate - callback(rating) when a star is clicked
 * @param {boolean} disabled - disable interaction
 * @param {number} size - font-size in rem
 */
const RatingStars = ({ rating = 0, onRate, disabled = false, size = 1.4 }) => {
  const [hovered, setHovered] = useState(0);

  const handleClick = useCallback(
    (value) => {
      if (!disabled && onRate) onRate(value);
    },
    [disabled, onRate]
  );

  return (
    <div className="star-input" role="group" aria-label="Rating stars">
      {[1, 2, 3, 4, 5].map((i) => (
        <button
          key={i}
          type="button"
          className={`star-btn ${(hovered || rating) >= i ? 'active' : ''}`}
          style={{ fontSize: `${size}rem` }}
          onClick={() => handleClick(i)}
          onMouseEnter={() => !disabled && setHovered(i)}
          onMouseLeave={() => setHovered(0)}
          disabled={disabled}
          aria-label={`Rate ${i} star${i > 1 ? 's' : ''}`}
          title={`${i} star${i > 1 ? 's' : ''}`}
        >
          <FaStar />
        </button>
      ))}
    </div>
  );
};

export default RatingStars;
