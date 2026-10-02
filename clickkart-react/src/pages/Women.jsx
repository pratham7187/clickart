import CategoryPage from '../components/CategoryPage';

import sareeImg from '../assets/images/saree1.jpg';
import lehengaImg from '../assets/images/lehenga1.jpg';
import wJeansImg from '../assets/images/w_jeans1.jpg';
import kurtaImg from '../assets/images/kurta1.jpg';
import wshoeImg from '../assets/images/wshoe2.jpg';

const subcategories = [
  { filter: 'sarees',   image: sareeImg,   label: 'Sarees' },
  { filter: 'lehenga',  image: lehengaImg, label: 'Lehenga' },
  { filter: 'jeans',    image: wJeansImg,  label: 'Women Jeans' },
  { filter: 'kurtis',   image: kurtaImg,   label: 'Kurtis' },
  { filter: 'footwear', image: wshoeImg,   label: 'Footwear' },
];

const Women = () => (
  <CategoryPage
    categoryId={2}
    title="Women's Fashion"
    gridTitle="All Women's Products"
    subcategories={subcategories}
  />
);

export default Women;
