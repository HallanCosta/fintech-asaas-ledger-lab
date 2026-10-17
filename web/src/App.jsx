import { useCallback, useEffect, useMemo, useState } from 'react'
import { Badge } from './components/ui/badge.jsx'
import { Button } from './components/ui/button.jsx'
import { Card } from './components/ui/card.jsx'
import { Separator } from './components/ui/separator.jsx'

const internalApiKey = import.meta.env.VITE_ASAAS_INTERNAL_API_KEY

function asaasHeaders(headers = {}) {
  return {
    Accept: 'application/json',
    ...headers,
    ...(internalApiKey ? { 'X-Api-Key': internalApiKey } : {}),
  }
}

const transactionFilters = ['Todas', 'Entradas', 'Saídas']
const balanceFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateFormatter = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
const longDateFormatter = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' })
const frontendEnvironment = import.meta.env.VITE_ASAAS_ENVIRONMENT || 'Staging · Sandbox'
const defaultStatementTo = formatDateInput(new Date())
const defaultStatementFrom = formatDateInput(new Date(Date.now() - 6 * 86400000))

function formatBRL(value) {
  return typeof value === 'number' && Number.isFinite(value) ? balanceFormatter.format(value) : '—'
}

function formatDateInput(date) {
  return date.toISOString().slice(0, 10)
}

function formatDateTime(value) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : dateFormatter.format(date)
}

async function readApiError(response, fallback) {
  if (response.status === 401) {
    return 'A API recusou a credencial. Defina VITE_ASAAS_INTERNAL_API_KEY em web/.env.local com o mesmo valor de ASAAS_INTERNAL_API_KEY.'
  }

  let payload = null
  try {
    payload = await response.json()
  } catch {
    // Algumas falhas podem não trazer um corpo JSON.
  }

  if (response.status === 502 && payload?.upstreamStatus === 401) {
    return 'A credencial do Asaas foi rejeitada. Gere uma nova chave no ambiente selecionado.'
  }
  if (response.status === 429 || response.status === 503) {
    return 'O Asaas está temporariamente indisponível. Tente novamente em instantes.'
  }
  return fallback
}

function mapTransaction(transaction, index) {
  const direction = transaction.direction === 'CREDIT' ? 'in' : 'out'
  const amount = Number(transaction.amount?.amount)
  const sign = direction === 'in' ? '+' : '-'
  const externalId = transaction.externalId?.value || `statement-${index}`

  return {
    id: externalId,
    type: direction === 'in' ? 'Entrada' : 'Saída',
    direction,
    title: transaction.description || 'Movimentação Asaas',
    detail: `${transaction.accountId || 'Conta Asaas'} · ${externalId}`,
    amount: `${sign} ${formatBRL(amount)}`,
    status: 'Processado',
    date: formatDateTime(transaction.occurredAt),
  }
}

function LedgerLogo() {
  return (
    <span className="ledger-logo" aria-label="Ledger Lab">
      <span className="ledger-logo-mark" aria-hidden="true"><i /><i /><i /></span>
      <span className="ledger-logo-copy">
        <strong>Ledger</strong>
        <span>Lab</span>
      </span>
    </span>
  )
}

function SearchIcon() {
  return <span className="search-icon" aria-hidden="true" />
}

function Chevron({ direction = 'down' }) {
  return <span className={`chevron chevron-${direction}`} aria-hidden="true" />
}

function WalletIcon() {
  return <span className="wallet-icon" aria-hidden="true"><i /></span>
}

function PulseDot() {
  return <span className="pulse-dot" aria-hidden="true" />
}

function LedgerHeader({ connectionState }) {
  const connectionLabel = connectionState === 'success'
    ? 'Sincronizado agora'
    : connectionState === 'loading'
      ? 'Sincronizando…'
      : 'Conexão indisponível'

  return (
    <header className="ledger-header">
      <div className="ledger-header-inner">
        <a className="ledger-brand-link" href="#overview" aria-label="Ledger Lab - início">
          <LedgerLogo />
          <span className="brand-tagline">Financial systems lab</span>
        </a>

        <nav className="ledger-nav" aria-label="Navegação principal">
          <a className="active" href="#overview">Visão geral</a>
          <a href="#events">Eventos <span className="nav-count">12</span></a>
          <a href="#reconciliation">Reconciliação</a>
        </nav>

        <div className="ledger-header-actions">
          <Button className="account-selector" type="button" variant="ghost">
            <WalletIcon />
            <span><small>Conta monitorada</small>Conta principal</span>
            <Chevron />
          </Button>
          <span className={`connection-status connection-status-${connectionState}`}><PulseDot /> {connectionLabel}</span>
        </div>
      </div>
    </header>
  )
}

function MetricCard({ label, value, detail, icon, tone = 'green', state = 'ready' }) {
  return (
    <Card className={`metric-card metric-${tone}`}>
      <div className="metric-topline">
        <span>{label}</span>
        <span className={`metric-icon metric-icon-${tone}`} aria-hidden="true">{icon}</span>
      </div>
      <strong className={`metric-value metric-value-${state}`}>{value}</strong>
      <span className="metric-detail">{detail}</span>
    </Card>
  )
}

function FilterButton({ active, children, onClick }) {
  return (
    <Button
      aria-pressed={active}
      className={`transaction-filter ${active ? 'active' : ''}`}
      onClick={onClick}
      type="button"
      variant="ghost"
    >
      {children}
    </Button>
  )
}

function TransactionRow({ transaction }) {
  return (
    <div className="transaction-row" role="row">
      <div className="transaction-description" role="cell">
        <span className={`transaction-icon transaction-icon-${transaction.direction}`} aria-hidden="true">
          {transaction.direction === 'in' ? '↓' : '↑'}
        </span>
        <span>
          <strong>{transaction.title}</strong>
          <small>{transaction.detail}</small>
        </span>
      </div>
      <span className={`transaction-amount amount-${transaction.direction}`} role="cell">{transaction.amount}</span>
      <Badge variant={transaction.status === 'Pendente' ? 'warning' : 'success'}>{transaction.status}</Badge>
      <span className="transaction-date" role="cell">{transaction.date}</span>
      <Button aria-label={`Abrir ${transaction.title}`} className="transaction-more" type="button" variant="ghost">•••</Button>
    </div>
  )
}

function TransactionsPanel({ filter, onFilterChange, statement, statementError, statementState, onRetry }) {
  const [query, setQuery] = useState('')
  const filteredTransactions = useMemo(() => statement.filter((transaction) => {
    const matchesFilter = filter === 'Todas' || transaction.type === filter.slice(0, -1)
    const searchable = `${transaction.title} ${transaction.detail}`.toLowerCase()
    return matchesFilter && searchable.includes(query.toLowerCase())
  }), [filter, query, statement])

  return (
    <Card as="section" className="panel transactions-panel" aria-labelledby="transactions-title">
      <div className="panel-heading panel-heading-wide">
        <div>
          <span className="panel-kicker">Extrato normalizado</span>
          <h2 id="transactions-title">Movimentações recentes</h2>
        </div>
        <Button className="panel-link" disabled={statementState === 'loading'} onClick={onRetry} type="button" variant="ghost">Sincronizar extrato <span>↻</span></Button>
      </div>
      <div className="transactions-toolbar">
        <div className="filter-group" role="group" aria-label="Filtrar movimentações">
          {transactionFilters.map((item) => (
            <FilterButton active={filter === item} key={item} onClick={() => onFilterChange(item)}>{item}</FilterButton>
          ))}
        </div>
        <label className="transaction-search">
          <span className="sr-only">Pesquisar movimentações</span>
          <SearchIcon />
          <input aria-label="Pesquisar movimentações" onChange={(event) => setQuery(event.target.value)} placeholder="Pesquisar" type="search" value={query} />
        </label>
      </div>
      <div className="transaction-table" role="table">
        <div className="transaction-header" role="row">
          <span role="columnheader">Movimentação</span>
          <span role="columnheader">Valor</span>
          <span role="columnheader">Status</span>
          <span role="columnheader">Data</span>
          <span aria-hidden="true" />
        </div>
        {statementState === 'loading' && <div className="empty-transactions" role="status">Carregando extrato do Asaas…</div>}
        {statementState === 'error' && <div className="empty-transactions" role="alert">{statementError}</div>}
        {statementState === 'success' && filteredTransactions.length > 0 ? filteredTransactions.map((transaction) => (
          <TransactionRow key={transaction.id} transaction={transaction} />
        )) : statementState === 'success' && <div className="empty-transactions">Nenhuma movimentação encontrada.</div>}
      </div>
    </Card>
  )
}

function AccountPanel({ balance, balanceState, balanceError, onRetry }) {
  const isReady = balanceState === 'success'
  const isLoading = balanceState === 'loading'
  const isError = balanceState === 'error'

  return (
    <Card as="section" className="panel account-panel" aria-labelledby="account-title">
      <div className="account-panel-heading">
        <div className="account-icon"><WalletIcon /></div>
        <div>
          <span className="panel-kicker">Conta conectada</span>
          <h2 id="account-title">Conta Asaas</h2>
          <span className="account-number">API financeira · conta monitorada</span>
        </div>
        <Badge variant={isError ? 'warning' : 'success'}>{isError ? 'Indisponível' : 'Ativa'}</Badge>
      </div>
      <Separator />
      <span className="balance-label">Saldo disponível</span>
      {isReady && <strong className="balance-value">{formatBRL(balance.balance)}</strong>}
      {isLoading && <div className="balance-loading" role="status" aria-label="Carregando saldo"><i /><i /><i /></div>}
      {isError && (
        <div className="balance-error" role="alert">
          <strong>Saldo indisponível</strong>
          <span>{balanceError}</span>
          <Button className="retry-button" onClick={onRetry} type="button" variant="outline">Tentar novamente</Button>
        </div>
      )}
      {isReady && (
        <>
          <div className="balance-meta"><span>Saldo retornado pelo Asaas</span><span>100% disponível</span></div>
          <div className="balance-bar" aria-label="Saldo retornado pelo Asaas"><span style={{ width: '100%' }} /></div>
        </>
      )}
      <div className="account-details">
        <div><span>Última sincronização</span><strong>{isReady ? 'agora mesmo' : isLoading ? 'em andamento' : 'aguardando tentativa'}</strong></div>
        <div><span>Fonte de dados</span><strong>GET /api/asaas/balance</strong></div>
        <div><span>Ambiente</span><strong>{frontendEnvironment}</strong></div>
        <div><span>Endpoint externo</span><strong>GET /finance/balance</strong></div>
      </div>
    </Card>
  )
}

function PixPaymentPanel() {
  const [form, setForm] = useState({
    customer: '',
    value: '10.00',
    dueDate: formatDateInput(new Date(Date.now() + 86400000)),
    description: 'Cobrança de estudo',
  })
  const [payment, setPayment] = useState(null)
  const [paymentState, setPaymentState] = useState('idle')
  const [paymentError, setPaymentError] = useState('')

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setPaymentState('loading')
    setPaymentError('')
    setPayment(null)

    try {
      const paymentResponse = await fetch('/api/asaas/payments/pix', {
        method: 'POST',
        headers: asaasHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify({
          customer: form.customer,
          value: Number(form.value),
          dueDate: form.dueDate,
          description: form.description,
          externalReference: `pix-study-${Date.now()}`,
        }),
      })
      if (!paymentResponse.ok) {
        throw new Error(await readApiError(paymentResponse, 'Não foi possível criar a cobrança Pix.'))
      }

      const createdPayment = await paymentResponse.json()
      if (!createdPayment?.id) throw new Error('A API não retornou o identificador da cobrança')

      const qrResponse = await fetch(`/api/asaas/payments/${createdPayment.id}/pix-qrcode`, {
        headers: asaasHeaders(),
      })
      if (!qrResponse.ok) {
        throw new Error(await readApiError(qrResponse, 'Não foi possível consultar o QR Code Pix.'))
      }

      const qrCode = await qrResponse.json()
      setPayment({ ...createdPayment, qrCode })
      setPaymentState('success')
    } catch (error) {
      console.error('Falha ao criar cobrança Pix', error)
      setPaymentState('error')
      setPaymentError(error?.message || 'Não foi possível criar a cobrança Pix. Verifique os dados e tente novamente.')
    }
  }

  return (
    <Card as="section" className="panel pix-payment-panel" aria-labelledby="pix-payment-title">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Operação Pix</span>
          <h2 id="pix-payment-title">Criar cobrança Pix</h2>
        </div>
        <Badge variant="default">{frontendEnvironment}</Badge>
      </div>
      <form className="pix-payment-form" onSubmit={handleSubmit}>
        <label>Customer ID<input name="customer" onChange={updateField} placeholder="cus_..." required value={form.customer} /></label>
        <label>Valor<input min="0.01" name="value" onChange={updateField} required step="0.01" type="number" value={form.value} /></label>
        <label>Vencimento<input name="dueDate" onChange={updateField} required type="date" value={form.dueDate} /></label>
        <label className="pix-payment-description">Descrição<input name="description" onChange={updateField} value={form.description} /></label>
        <Button disabled={paymentState === 'loading'} type="submit" variant="primary">{paymentState === 'loading' ? 'Criando…' : 'Criar cobrança'}</Button>
      </form>
      {paymentState === 'error' && <div className="payment-feedback payment-feedback-error" role="alert">{paymentError}</div>}
      {paymentState === 'success' && payment && (
        <div className="payment-feedback payment-feedback-success" role="status">
          <strong>Cobrança criada: {payment.id}</strong>
          {payment.qrCode.encodedImage && <img alt="QR Code Pix da cobrança" src={`data:image/png;base64,${payment.qrCode.encodedImage}`} />}
          <label>Pix copia e cola<input readOnly value={payment.qrCode.payload || 'Payload não retornado'} /></label>
          <small>Confira o status da cobrança no Asaas; criar a cobrança não confirma o pagamento.</small>
        </div>
      )}
    </Card>
  )
}

function PipelinePanel() {
  const steps = [
    { label: 'API consultada', detail: 'Saldo e extrato Asaas', state: 'done' },
    { label: 'Normalização', detail: 'NormalizedTransaction', state: 'done' },
    { label: 'Persistência', detail: 'PostgreSQL · próxima etapa', state: 'current' },
    { label: 'Webhooks', detail: 'Idempotência · próxima etapa', state: 'current' },
  ]

  return (
    <Card as="section" className="panel pipeline-panel" id="events" aria-labelledby="pipeline-title">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Processamento</span>
          <h2 id="pipeline-title">Pipeline de entrada</h2>
        </div>
        <span className="live-label"><PulseDot /> fluxo de estudo</span>
      </div>
      <div className="pipeline-steps">
        {steps.map((step, index) => (
          <div className={`pipeline-step pipeline-step-${step.state}`} key={step.label}>
            <span className="pipeline-marker">{step.state === 'done' ? '✓' : '•'}</span>
            <span className="pipeline-copy"><strong>{step.label}</strong><small>{step.detail}</small></span>
            {index < steps.length - 1 && <span className="pipeline-line" aria-hidden="true" />}
          </div>
        ))}
      </div>
    </Card>
  )
}

function ReconciliationPanel() {
  return (
    <Card as="section" className="panel reconciliation-panel" id="reconciliation" aria-labelledby="reconciliation-title">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Conferência automática</span>
          <h2 id="reconciliation-title">Reconciliação</h2>
        </div>
        <Badge variant="default">Próxima etapa</Badge>
      </div>
      <div className="reconciliation-content">
        <div className="reconciliation-score"><strong>—</strong><span>aguardando persistência</span></div>
        <div className="reconciliation-stats"><div><strong>—</strong><span>movimentos</span></div><div><strong>—</strong><span>divergências</span></div></div>
      </div>
      <Separator />
      <div className="reconciliation-footer"><span>Disponível após persistência do extrato</span><Button className="panel-link" disabled type="button" variant="ghost">Abrir relatório <span>→</span></Button></div>
    </Card>
  )
}

function App() {
  const [filter, setFilter] = useState('Todas')
  const [balance, setBalance] = useState(null)
  const [balanceState, setBalanceState] = useState('loading')
  const [balanceError, setBalanceError] = useState('Não foi possível carregar o saldo da conta.')
  const [statement, setStatement] = useState([])
  const [statementState, setStatementState] = useState('loading')
  const [statementError, setStatementError] = useState('Não foi possível carregar o extrato da conta.')

  const loadBalance = useCallback(async () => {
    setBalanceState('loading')
    setBalanceError('')
    const controller = new AbortController()
    const timeoutId = window.setTimeout(() => controller.abort(), 15000)

    try {
      const response = await fetch('/api/asaas/balance', {
        headers: asaasHeaders(),
        signal: controller.signal,
      })
      if (!response.ok) {
        throw new Error(await readApiError(response, 'Não foi possível carregar o saldo.'))
      }

      const payload = await response.json()
      if (!payload || typeof payload.balance !== 'number') {
        throw new Error('Resposta de saldo inválida')
      }

      setBalance(payload)
      setBalanceState('success')
    } catch (error) {
      console.error('Falha ao carregar saldo da conta', error)
      setBalanceState('error')
      if (error?.name === 'AbortError') {
        setBalanceError('A API do Asaas não respondeu em 15 segundos. Tente novamente.')
      } else {
        setBalanceError(error?.message || 'Não foi possível carregar o saldo. Tente novamente.')
      }
    } finally {
      window.clearTimeout(timeoutId)
    }
  }, [])

  const loadStatement = useCallback(async () => {
    setStatementState('loading')
    setStatementError('')

    try {
      const response = await fetch(`/api/asaas/statement?from=${defaultStatementFrom}&to=${defaultStatementTo}`, {
        headers: asaasHeaders(),
      })
      if (!response.ok) {
        throw new Error(await readApiError(response, 'Não foi possível carregar o extrato.'))
      }

      const payload = await response.json()
      if (!Array.isArray(payload)) throw new Error('Resposta de extrato inválida')

      setStatement(payload.map(mapTransaction))
      setStatementState('success')
    } catch (error) {
      console.error('Falha ao carregar o extrato da conta', error)
      setStatementState('error')
      setStatementError(error?.message || 'Não foi possível carregar o extrato. Tente novamente.')
    }
  }, [])

  useEffect(() => {
    void loadBalance()
    void loadStatement()
  }, [loadBalance, loadStatement])

  const balanceValue = balanceState === 'success' ? formatBRL(balance?.balance) : balanceState === 'loading' ? 'Carregando…' : 'Indisponível'
  const environmentValue = balanceState === 'success' ? frontendEnvironment : balanceState === 'loading' ? '—' : 'Indisponível'
  const endpointValue = balanceState === 'success' ? 'HTTP 200' : balanceState === 'loading' ? '—' : 'Indisponível'
  const healthValue = balanceState === 'success' ? 'Conectada' : balanceState === 'loading' ? 'Verificando' : 'Indisponível'
  const healthDetail = balanceState === 'success' ? 'endpoint respondendo HTTP 200' : balanceState === 'loading' ? 'consultando o Asaas' : 'tentar novamente'

  return (
    <div className="ledger-shell">
      <LedgerHeader connectionState={balanceState} />
      <main className="ledger-main" id="overview">
        <div className="ledger-content">
          <section className="dashboard-intro">
            <div>
              <div className="breadcrumb"><span>LEDGER LAB</span><Chevron direction="right" /><span>VISÃO GERAL</span></div>
              <h1>Conta principal</h1>
              <p>Uma visão operacional do saldo, dos eventos e dos lançamentos normalizados.</p>
            </div>
            <div className="intro-meta"><span className="intro-date">{longDateFormatter.format(new Date())}</span><Button className="sync-button" disabled={balanceState === 'loading' || statementState === 'loading'} onClick={() => { void loadBalance(); void loadStatement() }} type="button" variant="outline"><span>↻</span> {balanceState === 'loading' || statementState === 'loading' ? 'Sincronizando…' : 'Sincronizar agora'}</Button></div>
          </section>

          <section className="metrics-grid" aria-label="Resumo da conta">
            <MetricCard detail={balanceState === 'success' ? 'fonte: API Asaas' : healthDetail} icon="◒" label="Saldo disponível" state={balanceState} tone="green" value={balanceValue} />
            <MetricCard detail="ambiente selecionado no runtime" icon="▣" label="Ambiente" state={balanceState} tone="blue" value={environmentValue} />
            <MetricCard detail="GET /finance/balance" icon="↑" label="Endpoint de saldo" state={balanceState} tone="amber" value={endpointValue} />
            <MetricCard detail={healthDetail} icon="✓" label="Conexão com o Asaas" state={balanceState} tone="violet" value={healthValue} />
          </section>

          <section className="content-grid">
            <TransactionsPanel filter={filter} onFilterChange={setFilter} onRetry={loadStatement} statement={statement} statementError={statementError} statementState={statementState} />
            <AccountPanel balance={balance} balanceError={balanceError} balanceState={balanceState} onRetry={loadBalance} />
            <PixPaymentPanel />
            <PipelinePanel />
            <ReconciliationPanel />
          </section>
        </div>
      </main>
    </div>
  )
}

export default App
