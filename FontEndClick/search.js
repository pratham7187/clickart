(function () {
  "use strict";

  const MAX_SUGGESTIONS = 6;
  const SEARCH_DEBOUNCE_MS = 120;
  const FUSE_OPTIONS = {
    includeScore: true,
    threshold: 0.42,
    distance: 100,
    ignoreLocation: true,
    minMatchCharLength: 2,
    keys: [
      { name: "name", weight: 0.45 },
      { name: "subcategory", weight: 0.25 },
      { name: "categoryDisplayName", weight: 0.15 },
      { name: "categoryName", weight: 0.05 },
      { name: "description", weight: 0.10 }
    ]
  };

  let catalogPromise;
  let fuse;

  function loadCatalogOnce() {
    if (!catalogPromise) {
      catalogPromise = apiGetAllProducts(0, 500)
        .then(response => {
          const data = response && response.data;
          const products = Array.isArray(data) ? data : (data && data.content) || [];
          fuse = new Fuse(products, FUSE_OPTIONS);
          return products;
        })
        .catch(error => {
          catalogPromise = null;
          throw error;
        });
    }
    return catalogPromise;
  }

  function debounce(callback, wait) {
    let timeoutId;
    return function (...args) {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => callback.apply(this, args), wait);
    };
  }

  function createSuggestion(product, index) {
    const option = document.createElement("a");
    option.className = "search-suggestion";
    option.href = `product.html?id=${encodeURIComponent(product.id)}`;
    option.id = `search-suggestion-${index}`;
    option.setAttribute("role", "option");
    option.setAttribute("aria-selected", "false");

    const image = document.createElement("img");
    image.src = product.imageUrl || "images/image/logo.png";
    image.alt = "";
    image.addEventListener("error", () => {
      image.src = "images/image/logo.png";
    }, { once: true });

    const details = document.createElement("span");
    details.className = "search-suggestion__details";

    const name = document.createElement("span");
    name.className = "search-suggestion__name";
    name.textContent = product.name;

    const price = document.createElement("span");
    price.className = "search-suggestion__price";
    price.textContent = formatPrice(product.price);

    details.append(name, price);
    option.append(image, details);
    return option;
  }

  function initializeSearchForm(form, formIndex) {
    if (form.dataset.searchInitialized === "true") return;

    const input = form.querySelector(".input, input[type='search'], input[type='text']");
    if (!input) return;

    form.dataset.searchInitialized = "true";
    form.classList.add("search-autocomplete");

    const dropdown = document.createElement("div");
    dropdown.className = "search-suggestions";
    dropdown.id = `search-suggestions-${formIndex}`;
    dropdown.setAttribute("role", "listbox");
    dropdown.hidden = true;
    form.appendChild(dropdown);

    input.setAttribute("autocomplete", "off");
    input.setAttribute("role", "combobox");
    input.setAttribute("aria-autocomplete", "list");
    input.setAttribute("aria-controls", dropdown.id);
    input.setAttribute("aria-expanded", "false");

    let suggestions = [];
    let activeIndex = -1;

    function closeDropdown() {
      dropdown.hidden = true;
      input.setAttribute("aria-expanded", "false");
      input.removeAttribute("aria-activedescendant");
      activeIndex = -1;
    }

    function setActiveSuggestion(index) {
      if (!suggestions.length) return;
      activeIndex = (index + suggestions.length) % suggestions.length;
      suggestions.forEach((suggestion, suggestionIndex) => {
        const isActive = suggestionIndex === activeIndex;
        suggestion.classList.toggle("is-active", isActive);
        suggestion.setAttribute("aria-selected", String(isActive));
      });
      input.setAttribute("aria-activedescendant", suggestions[activeIndex].id);
      suggestions[activeIndex].scrollIntoView({ block: "nearest" });
    }

    function showMessage(message, className) {
      dropdown.replaceChildren();
      const status = document.createElement("div");
      status.className = `search-suggestions__status ${className || ""}`.trim();
      status.textContent = message;
      dropdown.appendChild(status);
      suggestions = [];
      dropdown.hidden = false;
      input.setAttribute("aria-expanded", "true");
    }

    function renderResults(query) {
      const normalizedQuery = query.trim();
      if (!normalizedQuery) {
        closeDropdown();
        dropdown.replaceChildren();
        return;
      }

      loadCatalogOnce()
        .then(() => {
          if (input.value.trim() !== normalizedQuery) return;

          const products = fuse.search(normalizedQuery, { limit: MAX_SUGGESTIONS })
            .map(result => result.item);

          dropdown.replaceChildren();
          activeIndex = -1;

          if (!products.length) {
            showMessage(`No products found for "${normalizedQuery}".`, "is-empty");
            return;
          }

          products.forEach((product, index) => {
            dropdown.appendChild(createSuggestion(product, index));
          });
          suggestions = Array.from(dropdown.querySelectorAll(".search-suggestion"));
          dropdown.hidden = false;
          input.setAttribute("aria-expanded", "true");
        })
        .catch(error => {
          console.error("Could not load the search catalog:", error);
          showMessage("Search is temporarily unavailable.", "is-error");
        });
    }

    const renderDebounced = debounce(renderResults, SEARCH_DEBOUNCE_MS);

    input.addEventListener("focus", () => {
      loadCatalogOnce().catch(() => {});
      if (input.value.trim()) renderResults(input.value);
    });

    input.addEventListener("input", () => renderDebounced(input.value));

    input.addEventListener("keydown", event => {
      if (event.key === "ArrowDown") {
        if (!dropdown.hidden && suggestions.length) {
          event.preventDefault();
          setActiveSuggestion(activeIndex + 1);
        }
      } else if (event.key === "ArrowUp") {
        if (!dropdown.hidden && suggestions.length) {
          event.preventDefault();
          setActiveSuggestion(activeIndex - 1);
        }
      } else if (event.key === "Enter") {
        if (!dropdown.hidden && suggestions.length) {
          event.preventDefault();
          (suggestions[activeIndex] || suggestions[0]).click();
        }
      } else if (event.key === "Escape") {
        event.preventDefault();
        closeDropdown();
      }
    });

    form.addEventListener("submit", event => {
      event.preventDefault();
      if (!dropdown.hidden && suggestions.length) {
        (suggestions[activeIndex] || suggestions[0]).click();
      } else if (input.value.trim()) {
        renderResults(input.value);
      }
    });

    document.addEventListener("click", event => {
      if (!form.contains(event.target)) closeDropdown();
    });
  }

  function initializeSearch() {
    if (typeof Fuse === "undefined" || typeof apiGetAllProducts !== "function") {
      console.error("ClickKart search requires Fuse.js and api.js.");
      return;
    }

    document.querySelectorAll(".search__form").forEach(initializeSearchForm);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initializeSearch, { once: true });
  } else {
    initializeSearch();
  }

  window.initializeSearch = initializeSearch;
})();
