import CategoryPage from '../components/CategoryPage';

import ktshirtImg from '../assets/images/ktshirt1.jpg';
import kjeansImg from '../assets/images/kjeans1.jpg';
import kshoeImg from '../assets/images/kshoe2.jpg';

const subcategories = [
  { filter: 'upperwear',  image: ktshirtImg, label: 'Upperwear' },
  { filter: 'bottomwear', image: kjeansImg,  label: 'Bottom Wear' },
  { filter: 'footwear',   image: kshoeImg,   label: 'Footwear' },
];

const Kids = () => (
  <CategoryPage
    categoryId={3}
    title="Kids' Fashion"
    gridTitle="All Kids' Products"
    subcategories={subcategories}
  />
);

export default Kids;
