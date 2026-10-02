import Sidebar from './Sidebar';
import Navbar from './Navbar';
import Footer from './Footer';

const Layout = ({ children }) => {
  return (
    <>
      <Sidebar />
      <div className="main-content">
        <Navbar />
        {children}
        <Footer />
      </div>
    </>
  );
};

export default Layout;
