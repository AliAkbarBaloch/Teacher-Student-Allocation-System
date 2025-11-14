import {useTranslation} from "react-i18next"; // import translation hook

function App() {

  // call the hook 
  const {t} = useTranslation();

  return (
    <>
      <h1 className="text-3xl min-h-screen flex items-center justify-center font-bold underline">
        {t('app.title')}
      </h1>
    </>
  );
}

export default App
