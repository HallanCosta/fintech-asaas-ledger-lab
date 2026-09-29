function Badge({ className = '', variant = 'default', ...props }) {
  return <span className={`status-pill status-pill-${variant} ${className}`.trim()} {...props} />
}

export { Badge }
