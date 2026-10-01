import { useCallback, useEffect, useMemo, useState } from 'react'
import { Badge } from './components/ui/badge.jsx'
import { Button } from './components/ui/button.jsx'
import { Card } from './components/ui/card.jsx'
import { Separator } from './components/ui/separator.jsx'

const transactions = [
  {
    id: 'pix-8192',
    type: 'Entrada',
    direction: 'in',
    title: 'PIX recebido',
    detail: 'Acme Tecnologia · ID 8192',
    amount: '+ R$ 4.800,00',
    status: 'Processado',
    date: '29 set, 10:42',
  },
  {
    id: 'payment-331',
    type: 'Saída',
    direction: 'out',
    title: 'Pagamento agendado',
    detail: 'AWS Brasil · boleto',
    amount: '- R$ 320,00',
    status: 'Pendente',
    date: '29 set, 09:18',
  },
  {
    id: 'pix-8177',
    type: 'Entrada',
    direction: 'in',
    title: 'PIX recebido',
    detail: 'Consultoria Oliveira · ID 8177',
    amount: '+ R$ 1.250,00',
    status: 'Processado',
    date: '28 set, 16:05',
  },
  {
    id: 'transfer-204',
    type: 'Saída',
    direction: 'out',
    title: 'Transferência enviada',
    detail: 'Folha de pagamento · lote 204',
    amount: '- R$ 2.140,00',
    status: 'Processado',
    date: '28 set, 11:30',
  },
  {
    id: 'pix-8161',
    type: 'Entrada',
    direction: 'in',
    title: 'PIX recebido',
    detail: 'Studio Norte · ID 8161',
    amount: '+ R$ 680,00',
    status: 'Processado',
    date: '27 set, 14:22',
  },
]

const transactionFilters = ['Todas', 'Entradas', 'Saídas']
const balanceFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function formatBRL(value) {
  return typeof value === 'number' && Number.isFinite(value) ? balanceFormatter.format(value) : '—'
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

function LedgerHeader() {
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
          <span className="connection-status"><PulseDot /> Sincronizado agora</span>
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

function TransactionsPanel({ filter, onFilterChange }) {
  const [query, setQuery] = useState('')
  const filteredTransactions = useMemo(() => transactions.filter((transaction) => {
    const matchesFilter = filter === 'Todas' || transaction.type === filter.slice(0, -1)
    const searchable = `${transaction.title} ${transaction.detail}`.toLowerCase()
    return matchesFilter && searchable.includes(query.toLowerCase())
  }), [filter, query])

  return (
    <Card as="section" className="panel transactions-panel" aria-labelledby="transactions-title">
      <div className="panel-heading panel-heading-wide">
        <div>
          <span className="panel-kicker">Extrato normalizado</span>
          <h2 id="transactions-title">Movimentações recentes</h2>
        </div>
        <Button className="panel-link" type="button" variant="ghost">Ver extrato completo <span>→</span></Button>
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
        {filteredTransactions.length > 0 ? filteredTransactions.map((transaction) => (
          <TransactionRow key={transaction.id} transaction={transaction} />
        )) : <div className="empty-transactions">Nenhuma movimentação encontrada.</div>}
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
        <Badge variant="success">Ativa</Badge>
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
        <div><span>Ambiente</span><strong>Sandbox Asaas</strong></div>
        <div><span>Endpoint externo</span><strong>GET /finance/balance</strong></div>
      </div>
    </Card>
  )
}

function PipelinePanel() {
  const steps = [
    { label: 'Evento recebido', detail: 'Webhook / polling', state: 'done' },
    { label: 'Normalização', detail: 'NormalizedTransaction', state: 'done' },
    { label: 'Ledger lançado', detail: 'Double-entry', state: 'done' },
    { label: 'Saldo projetado', detail: 'Projection atualizada', state: 'current' },
  ]

  return (
    <Card as="section" className="panel pipeline-panel" id="events" aria-labelledby="pipeline-title">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Processamento</span>
          <h2 id="pipeline-title">Pipeline de entrada</h2>
        </div>
        <span className="live-label"><PulseDot /> ao vivo</span>
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
        <Badge variant="success">Tudo certo</Badge>
      </div>
      <div className="reconciliation-content">
        <div className="reconciliation-score"><strong>100%</strong><span>conferido</span></div>
        <div className="reconciliation-stats"><div><strong>24</strong><span>movimentos</span></div><div><strong>0</strong><span>divergências</span></div></div>
      </div>
      <Separator />
      <div className="reconciliation-footer"><span>Última execução · hoje, 10:42</span><Button className="panel-link" type="button" variant="ghost">Abrir relatório <span>→</span></Button></div>
    </Card>
  )
}

function App() {
  const [filter, setFilter] = useState('Todas')
  const [balance, setBalance] = useState(null)
  const [balanceState, setBalanceState] = useState('loading')
  const [balanceError, setBalanceError] = useState('Não foi possível carregar o saldo da conta.')

  const loadBalance = useCallback(async () => {
    setBalanceState('loading')
    setBalanceError('')
    const controller = new AbortController()
    const timeoutId = window.setTimeout(() => controller.abort(), 15000)

    try {
      const response = await fetch('/api/asaas/balance', {
        headers: { Accept: 'application/json' },
        signal: controller.signal,
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
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
        setBalanceError('Não foi possível carregar o saldo. Tente novamente.')
      }
    } finally {
      window.clearTimeout(timeoutId)
    }
  }, [])

  useEffect(() => {
    void loadBalance()
  }, [loadBalance])

  const balanceValue = balanceState === 'success' ? formatBRL(balance?.balance) : balanceState === 'loading' ? 'Carregando…' : 'Indisponível'
  const environmentValue = balanceState === 'success' ? 'Sandbox' : balanceState === 'loading' ? '—' : 'Indisponível'
  const endpointValue = balanceState === 'success' ? 'HTTP 200' : balanceState === 'loading' ? '—' : 'Indisponível'
  const healthValue = balanceState === 'success' ? 'Conectada' : balanceState === 'loading' ? 'Verificando' : 'Indisponível'
  const healthDetail = balanceState === 'success' ? 'endpoint respondendo HTTP 200' : balanceState === 'loading' ? 'consultando o Asaas' : 'tentar novamente'

  return (
    <div className="ledger-shell">
      <LedgerHeader />
      <main className="ledger-main" id="overview">
        <div className="ledger-content">
          <section className="dashboard-intro">
            <div>
              <div className="breadcrumb"><span>LEDGER LAB</span><Chevron direction="right" /><span>VISÃO GERAL</span></div>
              <h1>Conta principal</h1>
              <p>Uma visão operacional do saldo, dos eventos e dos lançamentos normalizados.</p>
            </div>
            <div className="intro-meta"><span className="intro-date">29 de setembro de 2026</span><Button className="sync-button" disabled={balanceState === 'loading'} onClick={loadBalance} type="button" variant="outline"><span>↻</span> {balanceState === 'loading' ? 'Sincronizando…' : 'Sincronizar agora'}</Button></div>
          </section>

          <section className="metrics-grid" aria-label="Resumo da conta">
            <MetricCard detail={balanceState === 'success' ? 'fonte: API Asaas' : healthDetail} icon="◒" label="Saldo disponível" state={balanceState} tone="green" value={balanceValue} />
            <MetricCard detail="ambiente configurado em server/.env.local" icon="▣" label="Ambiente" state={balanceState} tone="blue" value={environmentValue} />
            <MetricCard detail="GET /finance/balance" icon="↑" label="Endpoint de saldo" state={balanceState} tone="amber" value={endpointValue} />
            <MetricCard detail={healthDetail} icon="✓" label="Conexão com o Asaas" state={balanceState} tone="violet" value={healthValue} />
          </section>

          <section className="content-grid">
            <TransactionsPanel filter={filter} onFilterChange={setFilter} />
            <AccountPanel balance={balance} balanceError={balanceError} balanceState={balanceState} onRetry={loadBalance} />
            <PipelinePanel />
            <ReconciliationPanel />
          </section>
        </div>
      </main>
    </div>
  )
}

export default App
