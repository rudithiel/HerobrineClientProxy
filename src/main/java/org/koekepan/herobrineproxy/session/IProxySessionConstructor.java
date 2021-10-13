package org.koekepan.herobrineproxy.session;

public interface IProxySessionConstructor {
	public IProxySessionNew createProxySession(String username);
	public IProxySessionNew getProxySession(String username);
}
