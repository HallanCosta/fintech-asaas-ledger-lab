import { useState } from 'react'

const transactions = [
  { id: 'pix-001', type: 'PIX recebido', amount: 'R$ 100,00', status: 'Processado', date: '12 abr 2024', direction: 'in' },
  { id: 'payment-001', type: 'Pagamento', amount: 'R$ 35,00', status: 'Pendente', date: '11 abr 2024', direction: 'out' },
  { id: 'pix-002', type: 'PIX recebido', amount: 'R$ 50,00', status: 'Processado', date: '10 abr 2024', direction: 'in' },
  { id: 'payment-002', type: 'Pagamento', amount: 'R$ 70,00', status: 'Processado', date: '09 abr 2024', direction: 'out' },
]

const eventTimeline = [
  ['Recebido', '12 abr 2024, 10:15'],
  ['Evento salvo', '12 abr 2024, 10:15'],
  ['Ledger lançado', '12 abr 2024, 10:16'],
  ['Saldo projetado', '12 abr 2024, 10:16'],
]

function Logo() {
  return <span className="logo-mark" aria-hidden="true"><i /><i /><i /></span>
}

function App() {
  const [selectedId, setSelectedId] = useState('pix-001')
  const selected = transactions.find((transaction) => transaction.id === selectedId) ?? transactions[0]

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <Logo />
          <div>
            <strong>Inter Ledger Lab</strong>
            <span>Explorando a arquitetura financeira com Java</span>
          </div>
        </div>

        <nav aria-label="Navegação principal">
          <a className="active" href="#overview">Visão geral</a>
          <a href="#events">Eventos</a>
          <a href="#reconciliation">Reconciliação</a>
        </nav>

        <div className="account-controls">
          <button className="account-select" type="button">▣ <span>Conta principal</span>⌄</button>
          <span className="sync-status"><b /> Sincronizado agora</span>
        </div>
      </header>

      <main className="workspace" id="overview">
        <section className="panel transactions-panel" aria-labelledby="transactions-title">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">CONTA PRINCIPAL</p>
              <h1 id="transactions-title">Transações recentes</h1>
            </div>
            <div className="balance-block">
              <span>Saldo atual</span>
              <strong>R$ 1.240,00</strong>
            </div>
          </div>

          <div className="transaction-list" role="list">
            <div className="list-header" aria-hidden="true"><span>Descrição</span><span>Valor</span><span>Status</span><span>Data</span></div>
            {transactions.map((transaction) => (
              <button
                className={`transaction-row ${transaction.id === selectedId ? 'selected' : ''}`}
                key={transaction.id}
                onClick={() => setSelectedId(transaction.id)}
                type="button"
              >
                <span className={`direction-icon ${transaction.direction}`}>{transaction.direction === 'in' ? '↓' : '↑'}</span>
                <span className="transaction-name">{transaction.type}</span>
                <span className="transaction-amount">{transaction.amount}</span>
                <span className={`status-pill ${transaction.status === 'Pendente' ? 'pending' : ''}`}>{transaction.status}</span>
                <span className="transaction-date">{transaction.date}</span>
              </button>
            ))}
          </div>
        </section>

        <section className="panel detail-panel" aria-labelledby="detail-title">
          <div className="detail-heading">
            <div className="detail-icon">↓</div>
            <div>
              <p className="eyebrow">TRANSAÇÃO SELECIONADA</p>
              <h2 id="detail-title">{selected.type}</h2>
              <span>{selected.date}</span>
            </div>
            <span className={`status-pill ${selected.status === 'Pendente' ? 'pending' : ''}`}>{selected.status}</span>
          </div>

          <div className="amount">{selected.amount}</div>

          <div className="section-divider" />
          <h3>Linha do tempo <small>Event Sourcing</small></h3>
          <ol className="timeline">
            {eventTimeline.map(([label, time]) => <li key={label}><b /><div><strong>{label}</strong><span>{time}</span></div></li>)}
          </ol>

          <div className="section-divider" />
          <h3>Lançamento contábil <small>Double-entry</small></h3>
          <div className="ledger-entries">
            <div><span className="entry-side debit">Débito</span><span>Conta Inter</span><strong>+ R$ 100,00</strong></div>
            <div><span className="entry-side credit">Crédito</span><span>Contrapartida</span><strong>- R$ 100,00</strong></div>
          </div>
        </section>
      </main>
    </div>
  )
}

export default App
