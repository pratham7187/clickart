// Fixed Universal Search Script - js/search.js
(function() {
  'use strict';

  // Initialize search functionality
  function initializeSearch() {
    console.log('🔍 Initializing search functionality...');
    
    // Function to perform search
    function performSearch(query) {
      if (!query || query.trim() === '') {
        alert('Please enter a search term');
        return;
      }
      
      const encodedQuery = encodeURIComponent(query.trim());
      console.log(`🔍 Searching for: ${query}`);
      window.location.href = `search.html?query=${encodedQuery}`;
    }

    // Wait a bit for DOM to be fully ready
    setTimeout(() => {
      // Find all search elements with multiple selectors
      const searchForms = document.querySelectorAll(
        '.search__form, form.search__form, .search-form'
      );
      
      const searchInputs = document.querySelectorAll(
        '.search__form .input, .search__form input[type="text"], .search-form input, input[placeholder*="Search"]'
      );
      
      const searchIcons = document.querySelectorAll(
        '.search__icon, .search__form .search__icon, .search-icon, .fa-magnifying-glass'
      );

      console.log(`📊 Found: ${searchForms.length} forms, ${searchInputs.length} inputs, ${searchIcons.length} icons`);

      // Handle form submissions (Enter key)
      searchForms.forEach((form, index) => {
        // Remove any existing listeners
        form.removeEventListener('submit', handleFormSubmit);
        form.addEventListener('submit', handleFormSubmit);
        
        function handleFormSubmit(e) {
          e.preventDefault();
          console.log(`📝 Form ${index} submitted`);
          
          const input = form.querySelector('.input') || 
                       form.querySelector('input[type="text"]') ||
                       form.querySelector('input');
          if (input) {
            performSearch(input.value);
          }
        }
      });

      // Handle search icon clicks
      searchIcons.forEach((icon, index) => {
        icon.style.cursor = 'pointer';
        
        // Remove existing listeners
        icon.removeEventListener('click', handleIconClick);
        icon.addEventListener('click', handleIconClick);
        
        function handleIconClick(e) {
          e.preventDefault();
          e.stopPropagation();
          console.log(`🖱️ Search icon ${index} clicked`);
          
          // Find the associated input field
          const form = icon.closest('.search__form') || 
                      icon.closest('form') || 
                      icon.closest('.navBar');
          
          const input = form ? (
            form.querySelector('.input') || 
            form.querySelector('input[type="text"]') ||
            form.querySelector('input')
          ) : null;
          
          if (input) {
            performSearch(input.value);
          } else {
            console.log('❌ No input found for search icon');
          }
        }
      });

      // Handle Enter key on input fields directly
      searchInputs.forEach((input, index) => {
        // Remove existing listeners
        input.removeEventListener('keypress', handleKeyPress);
        input.addEventListener('keypress', handleKeyPress);
        
        function handleKeyPress(e) {
          if (e.key === 'Enter') {
            e.preventDefault();
            console.log(`⌨️ Enter pressed on input ${index}`);
            performSearch(input.value);
          }
        }
      });

      // Add hover effects to search icons
      searchIcons.forEach(icon => {
        icon.addEventListener('mouseenter', function() {
          this.style.opacity = '0.7';
          this.style.transform = 'scale(1.1)';
        });
        
        icon.addEventListener('mouseleave', function() {
          this.style.opacity = '1';
          this.style.transform = 'scale(1)';
        });
      });

      console.log('✅ Search initialization complete!');
    }, 100);
  }

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeSearch);
  } else {
    initializeSearch();
  }

  // Also initialize after a short delay to catch dynamically loaded content
  setTimeout(initializeSearch, 500);
  setTimeout(initializeSearch, 1000); // Extra backup

  // Export for manual initialization if needed
  window.initializeSearch = initializeSearch;
  window.performSearch = function(query) {
    if (!query || query.trim() === '') {
      alert('Please enter a search term');
      return;
    }
    const encodedQuery = encodeURIComponent(query.trim());
    window.location.href = `search.html?query=${encodedQuery}`;
  };
})();