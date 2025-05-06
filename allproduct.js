const allProducts = [
    // Men's T-Shirts
    { name: "Men's T-Shirt 1", image: "images/men-tshirt1.jpg", price: "₹499" },
    { name: "Men's T-Shirt 2", image: "images/men-tshirt2.jpg", price: "₹549" },
    { name: "Men's T-Shirt 3", image: "images/men-tshirt3.jpg", price: "₹599" },
    { name: "Men's T-Shirt 4", image: "images/men-tshirt4.jpg", price: "₹649" },
    { name: "Men's T-Shirt 5", image: "images/men-tshirt5.jpg", price: "₹699" },
    { name: "Men's T-Shirt 6", image: "images/men-tshirt6.jpg", price: "₹749" },
  
    // Men's Formal Shirts
    { name: "Formal Shirt 1", image: "images/men-formal1.jpg", price: "₹799" },
    { name: "Formal Shirt 2", image: "images/men-formal2.jpg", price: "₹849" },
    { name: "Formal Shirt 3", image: "images/men-formal3.jpg", price: "₹899" },
    { name: "Formal Shirt 4", image: "images/men-formal4.jpg", price: "₹949" },
    { name: "Formal Shirt 5", image: "images/men-formal5.jpg", price: "₹999" },
    { name: "Formal Shirt 6", image: "images/men-formal6.jpg", price: "₹1049" },
  
    // Men's Jeans
    { name: "Men's Jeans 1", image: "images/men-jeans1.jpg", price: "₹1099" },
    { name: "Men's Jeans 2", image: "images/men-jeans2.jpg", price: "₹1149" },
    { name: "Men's Jeans 3", image: "images/men-jeans3.jpg", price: "₹1199" },
    { name: "Men's Jeans 4", image: "images/men-jeans4.jpg", price: "₹1249" },
    { name: "Men's Jeans 5", image: "images/men-jeans5.jpg", price: "₹1299" },
    { name: "Men's Jeans 6", image: "images/men-jeans6.jpg", price: "₹1349" },
  
    // Men's Joggers
    { name: "Men's Joggers 1", image: "images/men-jogger1.jpg", price: "₹899" },
    { name: "Men's Joggers 2", image: "images/men-jogger2.jpg", price: "₹949" },
    { name: "Men's Joggers 3", image: "images/men-jogger3.jpg", price: "₹999" },
    { name: "Men's Joggers 4", image: "images/men-jogger4.jpg", price: "₹1049" },
    { name: "Men's Joggers 5", image: "images/men-jogger5.jpg", price: "₹1099" },
    { name: "Men's Joggers 6", image: "images/men-jogger6.jpg", price: "₹1149" },
  
    // Men's Footwear
    { name: "Men's Footwear 1", image: "images/men-shoes1.jpg", price: "₹999" },
    { name: "Men's Footwear 2", image: "images/men-shoes2.jpg", price: "₹1049" },
    { name: "Men's Footwear 3", image: "images/men-shoes3.jpg", price: "₹1099" },
    { name: "Men's Footwear 4", image: "images/men-shoes4.jpg", price: "₹1149" },
    { name: "Men's Footwear 5", image: "images/men-shoes5.jpg", price: "₹1199" },
    { name: "Men's Footwear 6", image: "images/men-shoes6.jpg", price: "₹1249" },
  
    // Women's Sarees
    { name: "Saree 1", image: "images/women-saree1.jpg", price: "₹1499" },
    { name: "Saree 2", image: "images/women-saree2.jpg", price: "₹1599" },
    { name: "Saree 3", image: "images/women-saree3.jpg", price: "₹1699" },
    { name: "Saree 4", image: "images/women-saree4.jpg", price: "₹1799" },
    { name: "Saree 5", image: "images/women-saree5.jpg", price: "₹1899" },
    { name: "Saree 6", image: "images/women-saree6.jpg", price: "₹1999" },
  
    // Women's Lehengas
    { name: "Lehenga 1", image: "images/women-lehenga1.jpg", price: "₹2499" },
    { name: "Lehenga 2", image: "images/women-lehenga2.jpg", price: "₹2599" },
    { name: "Lehenga 3", image: "images/women-lehenga3.jpg", price: "₹2699" },
    { name: "Lehenga 4", image: "images/women-lehenga4.jpg", price: "₹2799" },
    { name: "Lehenga 5", image: "images/women-lehenga5.jpg", price: "₹2899" },
    { name: "Lehenga 6", image: "images/women-lehenga6.jpg", price: "₹2999" },
  
    // Women's Jeans
    { name: "Women's Jeans 1", image: "images/women-jeans1.jpg", price: "₹1099" },
    { name: "Women's Jeans 2", image: "images/women-jeans2.jpg", price: "₹1149" },
    { name: "Women's Jeans 3", image: "images/women-jeans3.jpg", price: "₹1199" },
    { name: "Women's Jeans 4", image: "images/women-jeans4.jpg", price: "₹1249" },
    { name: "Women's Jeans 5", image: "images/women-jeans5.jpg", price: "₹1299" },
    { name: "Women's Jeans 6", image: "images/women-jeans6.jpg", price: "₹1349" },
  
    // Kurtis
    { name: "Kurti 1", image: "images/women-kurti1.jpg", price: "₹899" },
    { name: "Kurti 2", image: "images/women-kurti2.jpg", price: "₹949" },
    { name: "Kurti 3", image: "images/women-kurti3.jpg", price: "₹999" },
    { name: "Kurti 4", image: "images/women-kurti4.jpg", price: "₹1049" },
    { name: "Kurti 5", image: "images/women-kurti5.jpg", price: "₹1099" },
    { name: "Kurti 6", image: "images/women-kurti6.jpg", price: "₹1149" },
  
    // Women's Footwear
    { name: "Women's Footwear 1", image: "images/women-shoes1.jpg", price: "₹999" },
    { name: "Women's Footwear 2", image: "images/women-shoes2.jpg", price: "₹1049" },
    { name: "Women's Footwear 3", image: "images/women-shoes3.jpg", price: "₹1099" },
    { name: "Women's Footwear 4", image: "images/women-shoes4.jpg", price: "₹1149" },
    { name: "Women's Footwear 5", image: "images/women-shoes5.jpg", price: "₹1199" },
    { name: "Women's Footwear 6", image: "images/women-shoes6.jpg", price: "₹1249" },
  
    // Kids Upperwear
    { name: "Kids Upperwear 1", image: "images/kids-upper1.jpg", price: "₹499" },
    { name: "Kids Upperwear 2", image: "images/kids-upper2.jpg", price: "₹549" },
    { name: "Kids Upperwear 3", image: "images/kids-upper3.jpg", price: "₹599" },
    { name: "Kids Upperwear 4", image: "images/kids-upper4.jpg", price: "₹649" },
    { name: "Kids Upperwear 5", image: "images/kids-upper5.jpg", price: "₹699" },
    { name: "Kids Upperwear 6", image: "images/kids-upper6.jpg", price: "₹749" },
  
    // Kids Bottomwear
    { name: "Kids Bottomwear 1", image: "images/kids-bottom1.jpg", price: "₹499" },
    { name: "Kids Bottomwear 2", image: "images/kids-bottom2.jpg", price: "₹549" },
    { name: "Kids Bottomwear 3", image: "images/kids-bottom3.jpg", price: "₹599" },
    { name: "Kids Bottomwear 4", image: "images/kids-bottom4.jpg", price: "₹649" },
    { name: "Kids Bottomwear 5", image: "images/kids-bottom5.jpg", price: "₹699" },
    { name: "Kids Bottomwear 6", image: "images/kids-bottom6.jpg", price: "₹749" },
  
    // Kids Footwear
    { name: "Kids Footwear 1", image: "images/kids-shoes1.jpg", price: "₹599" },
    { name: "Kids Footwear 2", image: "images/kids-shoes2.jpg", price: "₹649" },
    { name: "Kids Footwear 3", image: "images/kids-shoes3.jpg", price: "₹699" },
    { name: "Kids Footwear 4", image: "images/kids-shoes4.jpg", price: "₹749" },
    { name: "Kids Footwear 5", image: "images/kids-shoes5.jpg", price: "₹799" },
    { name: "Kids Footwear 6", image: "images/kids-shoes6.jpg", price: "₹849" },
  ];
  