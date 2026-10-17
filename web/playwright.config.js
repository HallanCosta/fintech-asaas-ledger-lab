import { defineConfig, devices } from '@playwright/test'

// Valor de teste, não é segredo. O spec espera o mesmo header X-Api-Key.
const internalApiKey = 'e2e-internal-api-key'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  reporter: process.env.CI ? 'github' : 'list',
  use: {
    baseURL: 'http://127.0.0.1:5173',
    trace: 'retain-on-failure',
  },
  webServer: {
    command: 'npm run dev -- --host 127.0.0.1',
    url: 'http://127.0.0.1:5173',
    reuseExistingServer: !process.env.CI,
    env: {
      VITE_ASAAS_INTERNAL_API_KEY: internalApiKey,
    },
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
})
