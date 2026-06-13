/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_AVISOS_S3_PUBLIC_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
