import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import styles from "./Login.module.css";

const Login = () => {
  const { login, loading, error, clearError, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  // Destinazione dopo login (se redirectato da PrivateRoute)
  const from = location.state?.from?.pathname || "/dashboard";

  // Se già autenticato, vai subito alla dashboard
  useEffect(() => {
    if (isAuthenticated) navigate(from, { replace: true });
  }, [isAuthenticated, navigate, from]);

  // Pulisce l'errore dal context quando l'utente riprende a digitare
  useEffect(() => {
    if (error) clearError();
  }, [email, password]); // eslint-disable-line

  const validate = () => {
    const errs = {};
    if (!email.trim()) {
      errs.email = "Campo obbligatorio";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errs.email = "Inserisci un'email valida";
    }
    if (!password) errs.password = "Campo obbligatorio";
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setFieldErrors(errs);
      return;
    }
    setFieldErrors({});
    try {
      await login(email.trim(), password);
      // La navigazione è gestita dall'useEffect che osserva isAuthenticated.
      // Non chiamare navigate() qui per evitare la race condition con il re-render.
    } catch {
      // errore già gestito in AuthContext (state `error`)
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        {/* Brand */}
        <div className={styles.brand}>
          <div className={styles.brandIcon}>BCC</div>
          <div>
            <h1 className={styles.brandTitle}>Blue Crystal Chicken</h1>
            <p className={styles.brandSub}>Pannello gestione Manager</p>
          </div>
        </div>

        {/* Errore globale dal backend */}
        {error && (
          <div className={styles.alertError} role="alert">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} noValidate className={styles.form}>
          <div className={styles.field}>
            <label htmlFor="email" className={styles.label}>
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              autoFocus
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={`${styles.input} ${
                fieldErrors.email ? styles.inputError : ""
              }`}
              placeholder="Inserisci email"
              disabled={loading}
            />
            {fieldErrors.email && (
              <span className={styles.fieldError}>{fieldErrors.email}</span>
            )}
          </div>

          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={`${styles.input} ${
                fieldErrors.password ? styles.inputError : ""
              }`}
              placeholder="••••••••"
              disabled={loading}
            />
            {fieldErrors.password && (
              <span className={styles.fieldError}>{fieldErrors.password}</span>
            )}
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={loading}
          >
            {loading ? (
              <span className={styles.spinner} aria-label="Caricamento" />
            ) : (
              "Accedi"
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
