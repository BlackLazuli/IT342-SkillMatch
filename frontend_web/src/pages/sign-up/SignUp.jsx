import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Checkbox from '@mui/material/Checkbox';
import CssBaseline from '@mui/material/CssBaseline';
import Divider from '@mui/material/Divider';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormLabel from '@mui/material/FormLabel';
import FormControl from '@mui/material/FormControl';
import { Link } from "react-router-dom";
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import MuiCard from '@mui/material/Card';
import { styled } from '@mui/material/styles';
import AppTheme from "../../theme/AppTheme";
import ColorModeSelect from '../../theme/ColorModeSelect';
import { GoogleIcon, FacebookIcon } from './components/CustomIcons';
import skillmatchLogo from '../../images/skillmatch.png';

const Card = styled(MuiCard)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignSelf: 'center',
  width: '100%',
  padding: theme.spacing(4),
  gap: theme.spacing(2),
  margin: 'auto',
  boxShadow:
    'hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px',
  [theme.breakpoints.up('sm')]: {
    width: '900px', // Increased width to fit more fields
  },
  ...theme.applyStyles('dark', {
    boxShadow:
      'hsla(220, 30%, 5%, 0.5) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.08) 0px 15px 35px -5px',
  }),
}));

export default function SignUp(props) {
  const handleSubmit = (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    console.log(Object.fromEntries(data.entries()));
  };

  return (
    <AppTheme {...props}>
      <CssBaseline enableColorScheme />
      <ColorModeSelect sx={{ position: 'fixed', top: '1rem', right: '1rem' }} />
      <Box sx={{
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 2,
      }}>
        <Card variant="outlined">
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <img src={skillmatchLogo} alt="SkillMatch Logo" style={{ width: 200, height: 100, objectFit: 'contain' }} />
          </Box>
          <Typography component="h1" variant="h4" sx={{ fontSize: 'clamp(2rem, 10vw, 2.15rem)' }}>
            Sign up
          </Typography>
          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Form Fields - Multi-column layout */}
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="firstName">First Name</FormLabel>
                  <TextField required name="firstName" id="firstName" placeholder="Jon" />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="middleName">Middle Name</FormLabel>
                  <TextField name="middleName" id="middleName" placeholder="Arya" />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="lastName">Last Name</FormLabel>
                  <TextField required name="lastName" id="lastName" placeholder="Snow" />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="birthdate">Birthdate</FormLabel>
                  <TextField required name="birthdate" id="birthdate" type="date" InputLabelProps={{ shrink: true }} />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="contactNumber">Contact Number</FormLabel>
                  <TextField required name="contactNumber" id="contactNumber" placeholder="123-456-7890" />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="email">Email</FormLabel>
                  <TextField required name="email" id="email" placeholder="your@email.com" />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="password">Password</FormLabel>
                  <TextField required name="password" id="password" placeholder="••••••" type="password" />
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth>
                  <FormLabel htmlFor="confirmPassword">Confirm Password</FormLabel>
                  <TextField required name="confirmPassword" id="confirmPassword" placeholder="••••••" type="password" />
                </FormControl>
              </Grid>
            </Grid>

            {/* Checkbox */}
            <FormControlLabel
              control={<Checkbox value="allowExtraEmails" color="primary" />}
              label="I want to receive updates via email."
            />

            {/* Submit Button */}
            <Button type="submit" fullWidth variant="contained">Sign up</Button>
          </Box>

          {/* Social Sign-up and Link */}
          <Divider><Typography sx={{ color: 'text.secondary' }}>or</Typography></Divider>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Button fullWidth variant="outlined" onClick={() => alert('Sign up with Google')} startIcon={<GoogleIcon />}>
              Sign up with Google
            </Button>
            <Button fullWidth variant="outlined" onClick={() => alert('Sign up with Facebook')} startIcon={<FacebookIcon />}>
              Sign up with Facebook
            </Button>
            <Typography sx={{ textAlign: 'center' }}>
              Already have an account? <Link to="/login">Sign in</Link>
            </Typography>
          </Box>
        </Card>
      </Box>
    </AppTheme>
  );
}
