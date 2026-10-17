import { expect, test } from '@playwright/test'

// Mesmo valor definido em webServer.env de playwright.config.js.
const internalApiKey = 'e2e-internal-api-key'
const qrImage = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='

const statement = [
  {
    externalId: { value: 'asaas-statement:credit-001' },
    accountId: 'asaas-account',
    amount: { amount: 123.45, currency: { currencyCode: 'BRL' } },
    direction: 'CREDIT',
    occurredAt: '2026-10-09T12:30:00Z',
    description: 'Cliente de estudo',
  },
  {
    externalId: { value: 'asaas-statement:debit-001' },
    accountId: 'asaas-account',
    amount: { amount: 25.5, currency: { currencyCode: 'BRL' } },
    direction: 'DEBIT',
    occurredAt: '2026-10-09T13:00:00Z',
    description: 'Transferência Pix',
  },
]

function expectInternalApiKey(route) {
  expect(route.request().headers()['x-api-key']).toBe(internalApiKey)
}

async function mockDashboardApi(page, { balanceHandler } = {}) {
  await page.route('**/api/asaas/balance', async (route) => {
    expectInternalApiKey(route)
    if (balanceHandler) {
      await balanceHandler(route)
      return
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ balance: 123.45 }) })
  })

  await page.route('**/api/asaas/statement**', async (route) => {
    expectInternalApiKey(route)
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(statement) })
  })
}

test('carrega saldo e extrato normalizado', async ({ page }) => {
  await mockDashboardApi(page)
  await page.goto('/')

  await expect(page.getByText('R$ 123,45').first()).toBeVisible()
  await expect(page.getByText('Cliente de estudo')).toBeVisible()
  await expect(page.getByText('Transferência Pix')).toBeVisible()

  await page.getByRole('button', { name: 'Saídas' }).click()
  await expect(page.getByText('Transferência Pix')).toBeVisible()
  await expect(page.getByText('Cliente de estudo')).not.toBeVisible()
})

test('permite tentar novamente quando o saldo falha', async ({ page }) => {
  let balanceCalls = 0
  await mockDashboardApi(page, {
    balanceHandler: async (route) => {
      balanceCalls += 1
      if (balanceCalls < 3) {
        await route.fulfill({ status: 502, contentType: 'application/json', body: JSON.stringify({ code: 'ASAAS_UPSTREAM_ERROR', upstreamStatus: 401 }) })
        return
      }
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ balance: 123.45 }) })
    },
  })
  await page.goto('/')

  await expect(page.getByText('Saldo indisponível')).toBeVisible()
  await expect(page.getByText('A credencial do Asaas foi rejeitada. Gere uma nova chave no ambiente selecionado.')).toBeVisible()
  await page.getByRole('button', { name: 'Tentar novamente' }).click()
  await expect(page.getByText('R$ 123,45').first()).toBeVisible()
  expect(balanceCalls).toBe(3)
})

test('exibe erro e permite sincronizar novamente quando o extrato falha', async ({ page }) => {
  let statementCalls = 0
  await page.route('**/api/asaas/balance', async (route) => {
    expectInternalApiKey(route)
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ balance: 123.45 }) })
  })
  await page.route('**/api/asaas/statement**', async (route) => {
    expectInternalApiKey(route)
    statementCalls += 1
    if (statementCalls < 3) {
      await route.fulfill({ status: 502, contentType: 'application/json', body: JSON.stringify({ code: 'ASAAS_UPSTREAM_ERROR', upstreamStatus: 401 }) })
      return
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(statement) })
  })

  await page.goto('/')
  await expect(page.getByText('A credencial do Asaas foi rejeitada. Gere uma nova chave no ambiente selecionado.')).toBeVisible()
  await page.getByRole('button', { name: 'Sincronizar extrato' }).click()
  await expect(page.getByText('Cliente de estudo')).toBeVisible()
  expect(statementCalls).toBe(3)
})

test('cria cobrança Pix e exibe o QR Code retornado', async ({ page }) => {
  await mockDashboardApi(page)
  await page.route('**/api/asaas/payments/pix', async (route) => {
    expectInternalApiKey(route)
    expect(route.request().method()).toBe('POST')
    const body = route.request().postDataJSON()
    expect(body.customer).toBe('cus_study_001')
    expect(body.value).toBe(10)
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ id: 'pay_study_001' }) })
  })
  await page.route('**/api/asaas/payments/pay_study_001/pix-qrcode', async (route) => {
    expectInternalApiKey(route)
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ encodedImage: qrImage, payload: '000201010212pix-study-payload', expirationDate: '2026-10-10' }),
    })
  })

  await page.goto('/')
  await page.getByLabel('Customer ID').fill('cus_study_001')
  await page.getByLabel('Valor').fill('10')
  await page.getByRole('button', { name: 'Criar cobrança' }).click()

  await expect(page.getByText('Cobrança criada: pay_study_001')).toBeVisible()
  await expect(page.getByAltText('QR Code Pix da cobrança')).toBeVisible()
  await expect(page.getByLabel('Pix copia e cola')).toHaveValue('000201010212pix-study-payload')
})
