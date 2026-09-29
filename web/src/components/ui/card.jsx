function Card({ as: Component = 'div', className = '', ...props }) {
  return <Component className={`ui-card ${className}`.trim()} {...props} />
}

function CardHeader({ as: Component = 'div', className = '', ...props }) {
  return <Component className={`ui-card-header ${className}`.trim()} {...props} />
}

function CardContent({ as: Component = 'div', className = '', ...props }) {
  return <Component className={`ui-card-content ${className}`.trim()} {...props} />
}

export { Card, CardHeader, CardContent }
