import CategoryPage from '../components/CategoryPage';

// Sub-category images
import tshirtImg from '../assets/images/tshirt.jpg';
import formalsImg from '../assets/images/formals.jpg';
import jeansImg from '../assets/images/jeans.jpg';
import joggersImg from '../assets/images/joggers.jpg';
import footwearImg from '../assets/images/footwear.jpg';

const subcategories = [
  { filter: 'tshirt',   image: tshirtImg,   label: 'T-Shirts' },
  { filter: 'formal',   image: formalsImg,  label: 'Formal Shirts' },
  { filter: 'jeans',    image: jeansImg,    label: "Men's Jeans" },
  { filter: 'joggers',  image: joggersImg,  label: 'Joggers' },
  { filter: 'footwear', image: footwearImg, label: 'Footwear' },
];

const Men = () => (
  <CategoryPage
    categoryId={1}
    title="Men's Fashion"
    gridTitle="All Men's Products"
    subcategories={subcategories}
  />
);

export default Men;
