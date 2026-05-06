# Krakend - API Gateway declarativo
# Nota: el diagrama menciona v2.10.2 pero la ultima en Docker Hub es 2.9.4
FROM devopsfaith/krakend:2.9.4

COPY krakend.json /etc/krakend/krakend.json

EXPOSE 8080

# Imagen base ya define el entrypoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/__health || exit 1
