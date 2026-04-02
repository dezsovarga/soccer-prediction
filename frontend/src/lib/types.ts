export interface UserDto {
  id: string;
  email: string;
  displayName: string;
  pictureUrl: string | null;
  role: string;
  isActive: boolean;
}

export interface ErrorResponse {
  error: string;
  code: string;
  details?: unknown;
}
