import './App.css'
import HomePage from './pages/HomePage'
import { KeycloakProvider } from './context/KeycloakContext'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import ErrorBoundary from './context/ErrorBoundary'

function App() {
    return (
        <KeycloakProvider>
            <BrowserRouter>
                <ErrorBoundary>
                    <Routes>
                        <Route
                            path="/"
                            element={
                                <Layout>
                                    <HomePage />
                                </Layout>
                            }
                        />
                    </Routes>
                </ErrorBoundary>
            </BrowserRouter>
        </KeycloakProvider>
    )
}

export default App