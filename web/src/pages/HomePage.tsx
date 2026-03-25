import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Paper from '@mui/material/Paper';
import DashboardIcon from '@mui/icons-material/Dashboard';
import { useAuth } from '../hooks/useAuth';
import { modules } from '../modules/registry';
import { ModuleTile } from '../components/common/ModuleTile';

export function HomePage() {
  const { user } = useAuth();

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 1 }}>
        Hi, {user?.displayName}!
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Welcome to your dashboard.
      </Typography>

      {modules.length > 0 ? (
        <Grid container spacing={{ xs: 2, sm: 3 }}>
          {modules.map((mod) => (
            <Grid key={mod.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <ModuleTile
                name={mod.name}
                description={mod.description}
                icon={mod.icon}
                path={mod.path}
                color={mod.color}
              />
            </Grid>
          ))}
        </Grid>
      ) : (
        <Paper
          sx={{
            p: { xs: 4, sm: 6 },
            textAlign: 'center',
            borderRadius: 4,
          }}
          elevation={0}
          variant="outlined"
        >
          <DashboardIcon sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            Your dashboard is ready!
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Modules will appear here as they become available.
          </Typography>
        </Paper>
      )}
    </Box>
  );
}
