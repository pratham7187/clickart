const loaderStyle = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  minHeight: '60vh',
  width: '100%',
  fontFamily: "'Poppins', sans-serif",
  color: '#555',
  gap: '12px',
};

const spinnerStyle = {
  width: '36px',
  height: '36px',
  border: '4px solid #eee',
  borderTopColor: '#e91e63',
  borderRadius: '50%',
  animation: 'ck-spin 0.8s linear infinite',
};

const Loader = ({ message = 'Loading ClickKart...' }) => {
  return (
    <>
      <style>{`@keyframes ck-spin { to { transform: rotate(360deg); } }`}</style>
      <div style={loaderStyle}>
        <div style={spinnerStyle}></div>
        <p>{message}</p>
      </div>
    </>
  );
};

export default Loader;
