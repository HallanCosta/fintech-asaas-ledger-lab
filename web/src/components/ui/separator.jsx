function Separator({ className = '', ...props }) {
  return <div aria-hidden="true" className={`section-divider ${className}`.trim()} {...props} />
}

export { Separator }
